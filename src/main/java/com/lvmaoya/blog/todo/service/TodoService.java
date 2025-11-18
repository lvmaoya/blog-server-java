package com.lvmaoya.blog.todo.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.todo.entity.Todo;
import com.lvmaoya.blog.todo.pojo.TodoQuery;
import com.lvmaoya.blog.todo.mapper.TodoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class TodoService extends ServiceImpl<TodoMapper, Todo> {

    @Resource
    private TodoMapper todoMapper;

    public IPage<Todo> getTodoList(TodoQuery todoQuery) {

        if (Objects.isNull(todoQuery)){
            return todoMapper.selectPage(new Page<>(1,999),null);
        }

        LambdaQueryWrapper<Todo> todoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        IPage<Todo> page = new Page<>(todoQuery.getPage(), todoQuery.getSize());

        // 根据 id 进行精确查询
        todoLambdaQueryWrapper.eq(todoQuery.getId() > 0,Todo::getId, todoQuery.getId());
        todoLambdaQueryWrapper.like(Objects.nonNull(todoQuery.getTaskName()),Todo::getTaskName, todoQuery.getTaskName());
        todoLambdaQueryWrapper.like(Objects.nonNull(todoQuery.getDescription()),Todo::getDescription, todoQuery.getDescription());
        todoLambdaQueryWrapper.eq(todoQuery.getProgress() > 0,Todo::getProgress, todoQuery.getProgress());
        todoLambdaQueryWrapper.eq(Objects.nonNull(todoQuery.getPriority()),Todo::getPriority, todoQuery.getPriority());
        todoLambdaQueryWrapper.eq(Objects.nonNull(todoQuery.getCategory()),Todo::getCategory, todoQuery.getCategory());
        todoLambdaQueryWrapper.eq(Objects.nonNull(todoQuery.getAssignee()),Todo::getAssignee, todoQuery.getAssignee());
        todoLambdaQueryWrapper.ge(Objects.nonNull(todoQuery.getCreatedStart()),Todo::getCreatedTime, todoQuery.getCreatedStart());
        todoLambdaQueryWrapper.le(Objects.nonNull(todoQuery.getCreatedEnd()),Todo::getCreatedTime, todoQuery.getCreatedEnd());
        todoLambdaQueryWrapper.ge(Objects.nonNull(todoQuery.getDueDateStart()),Todo::getDueDate, todoQuery.getDueDateStart());
        todoLambdaQueryWrapper.le(Objects.nonNull(todoQuery.getDueDateEnd()),Todo::getDueDate, todoQuery.getDueDateEnd());
        return todoMapper.selectPage(page,todoLambdaQueryWrapper);
    }

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
    public Todo saveOrUpdateTodo(Todo todo) {
        // 新增todo
        if (todo.getId() == 0){
            // 找到前一个节点
            String day = DateUtil.format(todo.getDueDate(), "yyyy-MM-dd");
            Todo lastTodo = baseMapper.selectOne(new LambdaQueryWrapper<Todo>().like(Todo::getDueDate, day).eq(Todo::getSiblingId,-1));

            boolean isSaved = save(todo);
            if (!isSaved) {
                throw new RuntimeException("保存 Todo 失败");
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
        // 更新 Todo
        boolean isUpdated = updateById(todo);
        if (!isUpdated) {
            // 抛出异常触发事务回滚
            throw new RuntimeException("更新 Todo 失败");
        }

        // 返回更新后的 Todo 对象
        return baseMapper.selectById(todo.getId());
    }
    @Transactional
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
