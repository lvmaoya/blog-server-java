package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Todo;
import com.lvmaoya.blog.domain.entity.User;
import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.mapper.TodoMapper;
import com.lvmaoya.blog.mapper.UserMapper;
import com.lvmaoya.blog.service.TodoService;
import com.lvmaoya.blog.service.UserService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TodoServiceImpl extends ServiceImpl<TodoMapper, Todo> implements TodoService {

    @Resource
    private TodoMapper todoMapper;
    @Override
    public List<Todo> getTodoList(String sortBy) {
        LambdaQueryWrapper<Todo> todoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(Objects.nonNull(sortBy) && sortBy.equals("completed")){
            todoLambdaQueryWrapper.orderByAsc(Todo::getCompleted);
        }
        return todoMapper.selectList(todoLambdaQueryWrapper);
    }
}
