package com.wff.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wff.common.utils.PageUtils;
import com.wff.common.utils.Query;
import com.wff.mall.product.dao.CategoryDao;
import com.wff.mall.product.entity.CategoryEntity;
import com.wff.mall.product.service.CategoryBrandRelationService;
import com.wff.mall.product.service.CategoryService;
import com.wff.mall.product.vo.Catalog3Vo;
import com.wff.mall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);

        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> {
                    return categoryEntity.getParentCid() == 0;
                }).map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                }).sorted((menu1, menu2) -> {
                    return menu1.getSort() - menu2.getSort();
                }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1????????????????????????????????????????????????????????????
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catlogId) {
        List<Long> paths = new ArrayList<>();

        findParentPath(catlogId, paths);
        Collections.reverse(paths);

        return paths.toArray(new Long[0]);
    }

    /**
     * ??????????????????????????????
     *
     * @param category
     */
//    @CacheEvict(value = "category", key = "'getLevel1Categorys'")
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("??????????????????" + Thread.currentThread());

        List<CategoryEntity> entityList = baseMapper.selectList(null);
        // ????????????????????????
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // ??????????????????????????? ?????????????????????????????????
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // ????????????????????????????????????
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // ?????????????????????????????????
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    //TODO ??????????????????
//    @Override

    /**
     * ?????????springCache????????????
     *
     * @return
     */
    @Deprecated
    public Map<String, List<Catelog2Vo>> getCatelogJsonOlder() {

        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.hasLength(catalogJSON)) {
            System.out.println("?????????????????????????????????");
            return getCatalogJsonFromDbWithRedissonLock();
        }

        System.out.println("????????????");
        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * ??????redisson??????????????????
     *
     * @return
     */
    @Deprecated
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        System.out.println("????????????????????????");
        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    /**
     * ??????redis??????????????????????????????
     *
     * @return
     */
    @Deprecated
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("????????????????????????");
            Map<String, List<Catelog2Vo>> dataFromDb = null;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Collections.singletonList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            System.out.println("????????????????????????");
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    @Deprecated
    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.hasLength(catalogJSON)) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("??????????????????" + Thread.currentThread());

        List<CategoryEntity> entityList = baseMapper.selectList(null);
        // ????????????????????????
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // ??????????????????????????? ?????????????????????????????????
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // ????????????????????????????????????
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // ?????????????????????????????????
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        redisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * ???????????????
     *
     * @return
     */
    @Deprecated
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        //TODO ???????????? synchronized JUC(lock) ???????????????????????????????????????

        synchronized (this) {
            return getDataFromDb();
        }
    }

    /**
     * ???????????????????????? CategoryEntity ???????????? parent_cid????????????
     */
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> entityList, Long parentCid) {

        return entityList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    private void findParentPath(Long catlogId, List<Long> paths) {
        paths.add(catlogId);
        CategoryEntity entity = this.getById(catlogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }
    }

    public List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map((menu) -> {
            menu.setChildren(getChildrens(menu, all));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }
}