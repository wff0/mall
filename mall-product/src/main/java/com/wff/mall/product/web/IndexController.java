package com.wff.mall.product.web;

import com.wff.mall.product.entity.CategoryEntity;
import com.wff.mall.product.service.CategoryService;
import com.wff.mall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/16 15:30
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        System.out.println(Thread.currentThread().getId());

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    //"index/catalog.json"
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("hello")
    public String hello() {
        RLock lock = redisson.getLock("my-lock");
        lock.lock();

        try {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException ignored) {
        } finally {
            System.out.println("释放锁" + Thread.currentThread().getId());
            lock.unlock();
        }

        return "Hello";
    }

    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        String writeValue = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.writeLock();
        try {
            rLock.lock();
            System.out.println("写锁加锁成功" + Thread.currentThread().getId());
            writeValue = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue", writeValue);
            Thread.sleep(30000);
        } catch (InterruptedException ignored) {
        } finally {
            System.out.println("写锁释放成功" + Thread.currentThread().getId());
            rLock.unlock();
        }

        return writeValue;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        String readValue = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功" + Thread.currentThread().getId());
            readValue = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception ignored) {
        } finally {
            System.out.println("读锁释放成功" + Thread.currentThread().getId());
            rLock.unlock();
        }

        return readValue;
    }

    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
//        park.acquire();
        boolean b = park.tryAcquire();
        if (b) {
            return "ok";
        } else {
            return "error";
        }
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();
        return "ok";
    }

    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();

        return "放假了";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();

        return id + "班的人都走了";
    }
}
