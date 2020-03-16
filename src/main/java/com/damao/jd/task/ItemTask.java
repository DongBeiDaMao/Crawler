package com.damao.jd.task;

import com.damao.jd.pojo.Item;
import com.damao.jd.service.ItemService;
import com.damao.jd.util.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.Date;
import java.util.List;

@Component
public class ItemTask {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private ItemService itemService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Scheduled(fixedDelay = 100*1000)
    public void itemTask() throws Exception{
        String url = "https://search.jd.com/Search?keyword=%E6%89%8B%E6%9C%BA&enc=utf-8&qrst=1&rt=1&" +
                "stop=1&vt=2&wq=shou%27ji&s=61&click=0&page=";

        //页号,用于拼接url,只能取1 ，3 ，5 ，7 ......
        int pageNum;

        for(int i = 7; i < 10; i += 2){
            pageNum = i;
            String html = httpUtils.doGetHtml(url + pageNum);
            parseAndPersist(html);
        }

        System.out.println("手机数据抓取成功！");
    }

    /**
     * 解析html并持久化数据
     * @param html
     */
    private void parseAndPersist(String html) throws IOException {
        Document dom = Jsoup.parse(html);

        //获取到商品的spu集合
        Elements spuElms = dom.select("div#J_goodsList > ul > li");

        //遍历商品的spu
        for (Element spuElm : spuElms) {
            long spu = Long.parseLong(spuElm.attr("data-spu"));

            //获取到商品的sku集合
            Elements skuElms = spuElm.select("li.ps-item");

            //对同一类商品的不同sku遍历
            //设置Item的属性，并持久化
            for(Element skuElm : skuElms){
                Item item = new Item();

                //获取到sku，保存属性
                long sku = Long.parseLong(skuElm.select("img").first().attr("data-sku"));
                item.setSku(sku);

                //若以持久化过当前商品则跳过
                List<Item> list = itemService.findAll(item);
                if(list.size() > 0) continue;;

                //保存spu，url
                item.setSpu(spu);
                item.setUrl("https://item.jd.com/" + sku +".html");

                String picUrl = "https:" + skuElm.select("img[data-sku]").
                        attr("data-lazy-img").replace("/n9/","/n1/");

                //这里有个BUG,最后一个url取不到，为空，会导致异常
                if(picUrl.equals("https:"))
                    continue;

                //保存图片
                String picName = httpUtils.doGetImage(picUrl);
                item.setPic("C:\\Users\\handa\\Desktop\\images\\" + picName);

                //获取当前sku价格
                String jsonPrice = httpUtils.doGetHtml("https://p.3.cn/prices/mgets?skuIds=J_" + sku);
                double price = MAPPER.readTree(jsonPrice).get(0).get("p").asDouble();

                //获取标题
                String skuHtml = httpUtils.doGetHtml(item.getUrl());
                String title = Jsoup.parse(skuHtml).select("div.sku-name").text();

                item.setPrice(price);
                item.setTitle(title);
                item.setCreated(new Date());
                item.setUpdated(item.getCreated());

                itemService.save(item);

            }
        }
    }

}
