package com.sipc.catalina.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {

        //在配置好的配置类中增加此段代码即可
//        ParameterBuilder ticketPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<Parameter>();
//        ticketPar.name("saas-auth").description("登录校验")//name表示名称，description表示描述
//                .modelRef(new ModelRef("string")).parameterType("header")
//                .required(false).defaultValue("Bearer ").build();//required表示是否必填，defaultvalue表示默认值
//        pars.add(ticketPar.build());//添加完此处一定要把下边的带***的也加上否则不生效


        return new Docket(DocumentationType.SWAGGER_2)
               // .globalOperationParameters(aParameters)
               // .ignoredParameterTypes(AuthDto.class)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sipc.catalina.controller"))
                .paths(PathSelectors.any())
                .build();


    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("api")
                .description("api")
                .version("1.0")
                .build();
    }

}
