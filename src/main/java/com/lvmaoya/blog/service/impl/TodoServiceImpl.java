package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Todo;
import com.lvmaoya.blog.mapper.TodoMapper;
import com.lvmaoya.blog.service.TodoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
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
        List<Todo> todos = todoMapper.selectList(null);
        Collections.sort(todos, new Comparator<Todo>() {
            @Override
            public int compare(Todo t1, Todo t2) {
                int prevIdCompare = Integer.compare(t1.getPrevId(), t2.getPrevId());
                return prevIdCompare;
            }
        });
        return todos;
    }

    @Transactional
    @Override
    public Boolean order(Integer id, Integer prevTodoId) {
        Todo currentTodo = todoMapper.selectById(id);

        // 当前项的前后两项
        Todo prevTodoFrom = null;
        Todo siblingTodoFrom = null;
        Todo prevTodoTo = null;
        Todo siblingTodoTo = null;
        if (currentTodo.getSiblingId() != -1) {
            prevTodoFrom = todoMapper.selectById(currentTodo.getPrevId());
            siblingTodoFrom = todoMapper.selectById(currentTodo.getSiblingId());

            prevTodoFrom.setSiblingId(siblingTodoFrom.getId());
            siblingTodoFrom.setPrevId(prevTodoFrom.getId());
        } else {
            prevTodoFrom = todoMapper.selectById(currentTodo.getPrevId());
            prevTodoFrom.setSiblingId(-1);
        }

        LambdaQueryWrapper<Todo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Todo::getPrevId,-1);

        if (prevTodoId > 0) {
            prevTodoTo = todoMapper.selectById(prevTodoId);
            siblingTodoTo = todoMapper.selectById(prevTodoTo.getSiblingId());

            prevTodoTo.setSiblingId(id);
            siblingTodoTo.setPrevId(id);
        } else {
            siblingTodoTo = todoMapper.selectOne(queryWrapper);
            siblingTodoTo.setPrevId(id);
        }


//        currentTodo.setPrevId(prevId);
//
//        currentTodo.setPrevId(-1);
//        currentTodo.setSiblingId(firstTodo.getId());
//
//        firstTodo.setPrevId(id);
//
//        todoMapper.update(firstTodo, null);
//        todoMapper.update(prevTodo, null);
//        if (silingTodo != null) {
//            todoMapper.update(silingTodo, null);
//        }
//        todoMapper.update(currentTodo, null);


        return null;
    }
}
