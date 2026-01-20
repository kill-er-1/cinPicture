package com.cin.cinpicturebackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json 配置
 * js精度有限如果返回数字就会导致最后两位为0
 * 所以加上这个配置将 Long 类型转为字符串返回
 * 免前端 JavaScript 因为数字精度限制而把尾部位数弄错。
 */
@JsonComponent
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置
     * Spring Boot 默认会提供一个 Jackson2ObjectMapperBuilder ，里面包含了 Boot 自动配置的一堆默认设置（比如时间格式、是否失败等）。
     * 你在这里用 builder 构建出一个 ObjectMapper ，并把它作为 Spring 容器里的 Bean。
     * Spring MVC 在把 Controller 返回值写成 HTTP JSON 响应时，会使用容器里的 ObjectMapper （通过 MappingJackson2HttpMessageConverter ），所以这里的配置会影响所有 JSON 输出。
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        // Jackson 的“模块”机制：用模块向 ObjectMapper 注册自定义序列化/反序列化器、混入等扩展。
        module.addSerializer(Long.class, ToStringSerializer.instance);
        // 指定：当序列化 包装类型 Long 时，用 ToStringSerializer 。
        // ToStringSerializer 的行为很简单：把值变成 value.toString() 输出到 JSON，因此 JSON 里会是 "1234567890123456789" 这种字符串，而不是数字。
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // - Long.TYPE 代表 基本类型 long 。
// - 这行确保 long 字段也同样会被转成字符串。
        objectMapper.registerModule(module);
        // 把规则装到 ObjectMapper 上，从此以后全局生效。
        return objectMapper;
    }
}
