package com.lvmaoya.blog.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Todo;
import com.lvmaoya.blog.domain.searchParams.TodoListSearchParams;
import com.lvmaoya.blog.mapper.TodoMapper;
import com.lvmaoya.blog.service.TodoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class TodoServiceImpl extends ServiceImpl<TodoMapper, Todo> implements TodoService {

    @Resource
    private TodoMapper todoMapper;

    @Override
    public IPage<Todo> getTodoList(TodoListSearchParams todoListSearchParams) {

        if (Objects.isNull(todoListSearchParams)){
            return todoMapper.selectPage(new Page<>(1,999),null);
        }

        LambdaQueryWrapper<Todo> todoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        IPage<Todo> page = new Page<>(todoListSearchParams.getPage(), todoListSearchParams.getSize());

        // 根据 id 进行精确查询
        todoLambdaQueryWrapper.eq(todoListSearchParams.getId() > 0,Todo::getId, todoListSearchParams.getId());
        todoLambdaQueryWrapper.like(Objects.nonNull(todoListSearchParams.getTaskName()),Todo::getTaskName, todoListSearchParams.getTaskName());
        todoLambdaQueryWrapper.like(Objects.nonNull(todoListSearchParams.getDescription()),Todo::getDescription, todoListSearchParams.getDescription());
        todoLambdaQueryWrapper.eq(todoListSearchParams.getProgress() > 0,Todo::getProgress, todoListSearchParams.getProgress());
        todoLambdaQueryWrapper.eq(Objects.nonNull(todoListSearchParams.getPriority()),Todo::getPriority, todoListSearchParams.getPriority());
        todoLambdaQueryWrapper.eq(Objects.nonNull(todoListSearchParams.getCategory()),Todo::getCategory, todoListSearchParams.getCategory());
        todoLambdaQueryWrapper.eq(Objects.nonNull(todoListSearchParams.getAssignee()),Todo::getAssignee, todoListSearchParams.getAssignee());
        todoLambdaQueryWrapper.ge(Objects.nonNull(todoListSearchParams.getCreatedStart()),Todo::getCreatedTime, todoListSearchParams.getCreatedStart());
        todoLambdaQueryWrapper.le(Objects.nonNull(todoListSearchParams.getCreatedEnd()),Todo::getCreatedTime, todoListSearchParams.getCreatedEnd());
        todoLambdaQueryWrapper.ge(Objects.nonNull(todoListSearchParams.getDueDateStart()),Todo::getDueDate, todoListSearchParams.getDueDateStart());
        todoLambdaQueryWrapper.le(Objects.nonNull(todoListSearchParams.getDueDateEnd()),Todo::getDueDate, todoListSearchParams.getDueDateEnd());
        return todoMapper.selectPage(page,todoLambdaQueryWrapper);
    }

    @Override
    public List<Todo> getCurrentTodoList() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        List<Todo> todos = baseMapper.selectList(new LambdaQueryWrapper<Todo>()
                .like(Todo::getDueDate, today));

        // 构建 id 到 Todo 的映射
        Map<Integer, Todo> todoMap = new HashMap<>();
        for (Todo todo : todos) {
            todoMap.put(todo.getId(), todo);
        }


        Todo current = todos.stream()
                .filter(todo -> todo.getPrevId() == -1)
                .findFirst()
                .orElse(null);

        // 构建链表结构
        List<Todo> result = new ArrayList<>();

        while (current != null) {
            result.add(current);
            current = todoMap.get(current.getSiblingId());
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean order(Integer id, Integer prevTodoId, Integer siblingTodoId) {
        Todo currentTodo = todoMapper.selectById(id);

        // 判断位置没有发生变化
        if (currentTodo.getPrevId() == prevTodoId || currentTodo.getSiblingId() == siblingTodoId){
            return false;
        }


        Todo prevTodoTo = todoMapper.selectById(prevTodoId);
        Todo siblingTodoTo = todoMapper.selectById(siblingTodoId);
        Todo prevTodoFrom = todoMapper.selectById(currentTodo.getPrevId());
        Todo siblingTodoFrom = todoMapper.selectById(currentTodo.getSiblingId());

        if (Objects.nonNull(prevTodoTo)){
            prevTodoTo.setSiblingId(id);
            todoMapper.updateById(prevTodoTo);
        }
        if (Objects.nonNull(siblingTodoTo)){
            siblingTodoTo.setPrevId(id);
            todoMapper.updateById(siblingTodoTo);
        }
        if (Objects.nonNull(prevTodoFrom)){
            prevTodoFrom.setSiblingId(currentTodo.getSiblingId());
            todoMapper.updateById(prevTodoFrom);
        }
        if (Objects.nonNull(siblingTodoFrom)){
            siblingTodoFrom.setPrevId(currentTodo.getPrevId());
            todoMapper.updateById(siblingTodoFrom);
        }


        currentTodo.setPrevId(prevTodoId);
        currentTodo.setSiblingId(siblingTodoId);
        todoMapper.updateById(currentTodo);

        return true;
    }
    @Transactional
    @Override
    public Boolean saveOrUpdateTodo(Todo todo) {
        // 新增todo
        if (todo.getId() == 0){
            // 找到前一个节点
            String day = DateUtil.format(todo.getDueDate(), "yyyy-MM-dd");
            Todo lastTodo = baseMapper.selectOne(new LambdaQueryWrapper<Todo>().like(Todo::getDueDate, day).eq(Todo::getSiblingId,-1));

            boolean isSaved = save(todo);
            if (!isSaved) {
                return false;
            }

            if (lastTodo != null) {
                lastTodo.setSiblingId(todo.getId());
                baseMapper.updateById(lastTodo);
                todo.setPrevId(lastTodo.getId());
                todo.setSiblingId(-1);
            } else {
                todo.setPrevId(-1);
                todo.setSiblingId(-1);
            }
        }
        if (todo.getPrevId() == 0 || todo.getSiblingId() == 0){
            throw new RuntimeException("表单错误");
        }
        return updateById(todo);
    }
    @Transactional
    @Override
    public Boolean deleteTodo(Integer id) {
        // 查询该id对应的todo
        Todo todo = todoMapper.selectById(id);
        if (Objects.isNull(todo)) return false;

        // 查询其前一个todo
        Todo prevTodo = todoMapper.selectById(todo.getPrevId());
        Todo siblingTodo = todoMapper.selectById(todo.getSiblingId());

        if (Objects.nonNull(prevTodo)){
            prevTodo.setSiblingId(todo.getSiblingId());
            todoMapper.updateById(prevTodo);
        }
        if (Objects.nonNull(siblingTodo)){
            siblingTodo.setPrevId(todo.getPrevId());
            todoMapper.updateById(siblingTodo);
        }
        todoMapper.deleteById(id);

        return true;
    }
}
