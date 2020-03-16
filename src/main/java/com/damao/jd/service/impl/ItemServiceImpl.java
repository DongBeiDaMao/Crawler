package com.damao.jd.service.impl;

import com.damao.jd.dao.ItemDao;
import com.damao.jd.pojo.Item;
import com.damao.jd.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemDao itemDao;


    @Override
    public void save(Item item) {
        itemDao.save(item);
    }

    @Override
    public List<Item> findAll(Item item) {
        Example<Item> example = Example.of(item);
        List<Item> list = itemDao.findAll(example);
        return list;
    }
}
