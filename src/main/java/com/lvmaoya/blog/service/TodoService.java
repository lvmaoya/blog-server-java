package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Todo;
import com.lvmaoya.blog.domain.searchParams.TodoListSearchParams;

import java.util.List;

public interface TodoService  extends IService<Todo> {
    IPage<Todo> getTodoList(TodoListSearchParams todoListSearchParams);

    List<Todo> getCurrentTodoList();

    Boolean order(Integer id, Integer prevId, Integer siblingTodoId);

    Boolean saveOrUpdateTodo(Todo todo);

    Boolean deleteTodo(Integer id);
}
