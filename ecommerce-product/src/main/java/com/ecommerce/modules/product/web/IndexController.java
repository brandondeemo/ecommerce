package com.ecommerce.modules.product.web;

import com.ecommerce.modules.product.entity.CategoryEntity;
import com.ecommerce.modules.product.service.CategoryService;
import com.ecommerce.modules.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;


    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {

        // TODO 1. 查出所有的 1级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categories();

        model.addAttribute("categories", categoryEntityList);

        /*
        视图解析器进行拼串:
        前缀 classpath: /templates/
        + index
        后缀 .html
         */
        return "index";
    }

    // /index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJSON() {

        Map<String, List<Catalog2Vo>> map = categoryService.getCatalogJSON();

        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 1. 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("myLock");

        lock.lock(); // 阻塞式等待，默认加锁时间为 30s，时间不够会自动续期
        /*
        lock.lock() 内可以自己设定锁的过期时间
        问题，在自定义锁的过期时间到了之后，不会自动续期
        // 1. 如果我们传递了锁的超时时间，就发送给 redis执行脚本，进行占锁，默认超时就是我们指定的时间
        // 2. 如果我们没有传递锁的超时时间, lock.lock();
        就使用 30 * 1000 (lockWatchingdogTimeout 看门狗的默认时间)
        只要占锁成功，就会启动一个定时任务[重新给锁设置过期时间，新的过期时间就是看门狗的默认时间]
        internalLockLeaseTime[看门狗时间] / 3， 10s

         */

        /*
        redisson解决了:
        1. 锁的自动续期，如果业务所需时间超长，运行期间自动给锁续上新的30 s
        不用担心 业务时间过长，锁自动过期被删掉
        2. 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认会在 30s 后自动删除
         */
        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }

        // 2. 加锁
        return "hello";
    }
}
