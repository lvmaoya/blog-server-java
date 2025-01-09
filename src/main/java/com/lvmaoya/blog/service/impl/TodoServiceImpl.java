package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Todo;
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
    public List<Todo> getTodoList(String sortBy) {
        LambdaQueryWrapper<Todo> todoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(sortBy) && sortBy.equals("completed")) {
            todoLambdaQueryWrapper.orderByAsc(Todo::getCompleted);
        }
        return todoMapper.selectList(todoLambdaQueryWrapper);
    }

    @Override
    public List<Todo> getCurrentTodoList() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        List<Todo> todos = baseMapper.selectList(new LambdaQueryWrapper<Todo>()
                .like(Todo::getCreatedTime, today));

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

    @Override
    public Boolean saveOrUpdateTodo(Todo todo) {
        // 新增todo
        if (Objects.isNull(todo.getId())){
            // 找到前一个节点
            Todo prevTodo = baseMapper.selectOne(new LambdaQueryWrapper<Todo>().eq(Todo::getSiblingId,-1));
            if (prevTodo != null) {
                // 更新前一个节点的 siblingId
                prevTodo.setSiblingId(todo.getId());
                baseMapper.updateById(prevTodo);
            }
        }
        return null;
    }
}
