package com.damao.jd.service;

import com.damao.jd.pojo.Item;

import java.util.List;

public interface ItemService {

    public void save(Item item);

    public List<Item> findAll(Item item);

}
