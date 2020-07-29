package com.bigdemo.demo.entity;

import lombok.Data;

/**
 * 这个你不用删除，
 * 这就是实体类，就是员工表的实体化类，
 * @data相当于是对参数的get和set方法，你也可以使用@getter和@setter，效果一样的。 上面导入了lombok包，这个包里面的data方法书写了参量的get和set方法
 * 可以理解为@data 就是给Empoyee 类添加了两个方法
 * 不是给类添加两个方法，而是给参量 private String employeeId;
 *     private String employeeName;  这俩数据库字段的get和set方法  每一个参量都会有一对get和set方法
 *     那就是localhost:8080/get/selectEmployeeByAddress
 *     你不要这样理解，你就直接理解是这个类要想获取到自己的字段是不是要。get(ziduan),想要设置字段的值，是不是可以进行set(字段)
 *     等下我给你看下具体的
 *     就是这个@data就相当于两个字段各自的get和set方法，ok
 *     你Sql语句呢
 *     localhost:8080/get/selectEmployeeByAddress
 *     body：{“”}
 *     如何实现的   sql吗？en
 *     这个仅仅是实体化，卧槽，还他妈有三层，咋讲啊
 *     你说下大体结构每个
 *
 *     你看啊，springboot大概是个四层结构：entity----dao---service---controller
 *     其中entity是实体类，dao是数据库连接方法类，service服务层，controller是控制器
 *     dao里面写的是你想要的执行功能函数的mapper文件，也就是你要写的的sql方法
 *     service是服务层，也就是你写的dao层中的方法要在这个服务层里面实现service是个接口代码，所以要写个实现类，也就是在service下
 *     建个serviceImp实现类，只实现service下的接口类
 *     controller是控制器，它的目的是为我建立一个能与前台进行通信的路径，使得我们写的service能够在各自的路径下去实现
 *
 *     最后dao层里面写的.mapper类，只是一个方法，如果想要具体sql实现，我们要在resource下建个mapper包，在mapper下
 *     写.xml文件写sql语句
 *     好了！慢慢的干货啊，感谢宇哥 你启动的时候都是build application。properties是吧
 *     我是debug，你直接run也行，我主要是看bug信息
 *     pom.xml里是依赖是吧
 *     对啊，这个就是springboot加载你需要的一些外包 比兔数据库连接池，噢噢噢噢
 *     这东西算MVC吧
 *     不是啊，springMVC就是把模型、视图 控制器 都结合在一起，进行整体分析，我说不清，你还是
 *     看网上介绍吧
 *
 *
 */

@Data
public class Employee {
    private String employeeId;
    private String employeeName;

}

