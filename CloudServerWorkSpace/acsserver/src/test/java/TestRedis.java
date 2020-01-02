import com.acs.util.redis.RedisCacheUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author winnerlbm
 * @date 2018年5月7日
 * @desc Redis数据库测试类
 */
/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-*.xml"})*/
public class TestRedis {
    private RedisCacheUtil redisCache;
    private static String key;
    private static String field;
    private static String value;

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("resource")
    @Before
    public void setUp() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-redis.xml");
        context.start();
        redisCache = (RedisCacheUtil) context.getBean("redisCache");
    }

    // 初始化 数据
    static {
        key = "tb_redis_test";
        field = "test-content";
        value = "一系列测试信息！";
    }

    // 测试增加数据
    @Test
    public void testHset() {
        redisCache.hashSet(key, field, value);
        System.out.println("数据保存成功！");
    }

    // 测试查询数据
    @Test
    public void testHget() {
        String re = redisCache.hashGet(key, field);
        System.out.println("得到的数据：" + re);
    }

    // 测试数据的数量
    @Test
    public void testHsize() {
        long size = redisCache.hashSize(key);
        System.out.println("数量为：" + size);
    }

}
