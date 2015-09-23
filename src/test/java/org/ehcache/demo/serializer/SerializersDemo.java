package org.ehcache.demo.serializer;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.CacheConfigurationBuilder;
import org.ehcache.config.ResourcePoolsBuilder;
import org.ehcache.config.SerializerConfiguration;
import org.ehcache.config.persistence.CacheManagerPersistenceConfiguration;
import org.ehcache.config.serializer.DefaultSerializerConfiguration;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.demo.model.Description;
import org.ehcache.demo.model.Employee;
import org.junit.Ignore;
import org.junit.Test;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by alsu on 18/09/15.
 */
public class SerializersDemo {
  
  @Test
  public void testTransientSerializer() throws Exception {
    // tag::transientSerializerGoodSample[]
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    CacheConfiguration<Long, String> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder()
        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES).offheap(1, MemoryUnit.MB))   // <1>
        .add(new DefaultSerializerConfiguration<String>(
            DumbTransientStringSerializer.class, SerializerConfiguration.Type.VALUE))   // <2>
        .buildConfig(Long.class, String.class);

    Cache<Long, String> fruitsCache = cacheManager.createCache("fruitsCache", cacheConfig);
    fruitsCache.put(1L, "apple");
    fruitsCache.put(2L, "orange");
    fruitsCache.put(3L, "mango");
    assertThat(fruitsCache.get(1L), is("apple"));   // <3>
    assertThat(fruitsCache.get(3L), is("mango"));
    assertThat(fruitsCache.get(2L), is("orange"));
    assertThat(fruitsCache.get(1L), is("apple"));
    // end::transientSerializerGoodSample[]
  }

  @Ignore
  @Test
  public void testTransientSerializerWithPersistentCache() throws Exception {
    // tag::transientSerializerBadSample[]
    CacheConfiguration<Long, String> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder()
        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES).offheap(1, MemoryUnit.MB).disk(2, MemoryUnit.MB, true))  // <1>
        .add(new DefaultSerializerConfiguration<String>(
            DumbTransientStringSerializer.class, SerializerConfiguration.Type.VALUE))
        .buildConfig(Long.class, String.class);

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/Users/alsu/terracotta")))   // <2>
        .withCache("fruitsCache", cacheConfig)
        .build(true);

    Cache<Long, String> fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);   // <3>
    fruitsCache.put(1L, "apple");
    fruitsCache.put(2L, "mango");
    fruitsCache.put(3L, "orange");   // <4>
    assertThat(fruitsCache.get(1L), is("apple"));   // <5>
    
    cacheManager.close();   // <6>
    cacheManager.init();    // <7>
    fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);   // <8>
    assertThat(fruitsCache.get(1L), is("apple"));   // <9>
    // end::transientSerializerBadSample[]
  }

  @Test
  public void testPersistentSerializer() throws Exception {
    // tag::persistentSerializerGoodSample[]
    CacheConfiguration<Long, String> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder()
        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES).offheap(1, MemoryUnit.MB).disk(2, MemoryUnit.MB, true))
        .add(new DefaultSerializerConfiguration<String>(
            DumbPersistentStringSerializer.class, SerializerConfiguration.Type.VALUE))    // <1>
        .buildConfig(Long.class, String.class);

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/Users/alsu/terracotta")))
        .withCache("fruitsCache", cacheConfig)
        .build(true);

    Cache<Long, String> fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);
    fruitsCache.put(1L, "apple");
    fruitsCache.put(2L, "mango");
    fruitsCache.put(3L, "orange");
    assertThat(fruitsCache.get(1L), is("apple"));

    cacheManager.close();
    cacheManager.init();
    fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);
    assertThat(fruitsCache.get(1L), is("apple"));
    // end::persistentSerializerGoodSample[]
  }
  
  @Test
  public void testKryoSerializer() throws Exception {
    // tag::thirdPartySerializer[]
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    CacheConfiguration<Long, Employee> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder()
        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES).offheap(1, MemoryUnit.MB))
        .add(new DefaultSerializerConfiguration<Employee>(
            KryoSerializer.class, SerializerConfiguration.Type.VALUE))
        .buildConfig(Long.class, Employee.class);

    Cache<Long, Employee> employeeCache = cacheManager.createCache("employeeCache", cacheConfig);
    Employee emp =  new Employee(1234, "foo", 23, new Description("bar", 879));
    employeeCache.put(1L, emp);
    assertThat(employeeCache.get(1L), is(emp));
    // end::thirdPartySerializer[]
  }

  @Test
  public void testTransientKryoSerializer() throws Exception {
    // tag::transientKryoSerializer[]
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    CacheConfiguration<Long, Employee> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder()
        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES).offheap(1, MemoryUnit.MB))
        .add(new DefaultSerializerConfiguration<Employee>(
            TransientKryoSerializer.class, SerializerConfiguration.Type.VALUE))
        .buildConfig(Long.class, Employee.class);

    Cache<Long, Employee> employeeCache = cacheManager.createCache("employeeCache", cacheConfig);
    Employee emp =  new Employee(1234, "foo", 23, new Description("bar", 879));
    employeeCache.put(1L, emp);
    assertThat(employeeCache.get(1L), is(emp));
    // end::transientKryoSerializer[]
  }

  @Test
  public void testPersistentKryoSerializer() throws Exception {
    // tag::persistentKryoSerializer[]
    CacheConfiguration<Long, Employee> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder()
        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(10, EntryUnit.ENTRIES).offheap(1, MemoryUnit.MB).disk(2, MemoryUnit.MB, true))
        .add(new DefaultSerializerConfiguration<Employee>(
            PersistentKryoSerializer.class, SerializerConfiguration.Type.VALUE))
        .buildConfig(Long.class, Employee.class);

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/Users/alsu/terracotta")))
        .withCache("employeeCache", cacheConfig)
        .build(true);

    Cache<Long, Employee> employeeCache = cacheManager.getCache("employeeCache", Long.class, Employee.class);
    Employee emp =  new Employee(1234, "foo", 23, new Description("bar", 879));
    employeeCache.put(1L, emp);
    assertThat(employeeCache.get(1L), is(emp));

    cacheManager.close();
    cacheManager.init();
    employeeCache = cacheManager.getCache("employeeCache", Long.class, Employee.class);
    assertThat(employeeCache.get(1L), is(emp));
    // end::persistentKryoSerializer[]
  }

}
