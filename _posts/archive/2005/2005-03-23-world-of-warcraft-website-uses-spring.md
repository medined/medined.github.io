---
layout: post
title: World of Warcraft Website Uses Spring!
date: '2005-03-23T20:43:00.000-05:00'
author: David Medinets
categories:
- "[[java]]"
modified_time: '2006-07-03T09:21:04.293-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-111162870031810904
blogger_orig_url: http://affy.blogspot.com/2005/03/world-of-warcraft-website-uses-spring.md
year: 2005
theme: java
---

The World of Warcraft site is having problems. I got the following Java exception


<pre>

org.springframework.jdbc.CannotGetJdbcConnectionException: Could not get JDBC connection; nested exception is java.sql.SQLException: ORA-01017: invalid username/password; logon denied

 org.springframework.jdbc.datasource.DataSourceUtils.getConnection(DataSourceUtils.java:155)
 org.springframework.jdbc.datasource.DataSourceUtils.getConnection(DataSourceUtils.java:128)
 org.springframework.jdbc.core.JdbcTemplate.execute(JdbcTemplate.java:616)
 org.springframework.jdbc.core.JdbcTemplate.call(JdbcTemplate.java:653)
 org.springframework.jdbc.object.StoredProcedure.execute(StoredProcedure.java:100)
 com.blizzard.account.db.procedures.AccDBIStoredProcedure.safeExecute(AccDBIStoredProcedure.java:33)
 com.blizzard.account.db.procedures.AccGetAccIdByAccNameSP.execute(AccGetAccIdByAccNameSP.java:51)
 com.blizzard.account.db.AccountManager.getAccIdByAccName(AccountManager.java:720)
 com.blizzard.wow.web.loginSupport.action.controller.form.RequestPasswordFormController.onBind(RequestPasswordFormController.java:288)
 org.springframework.web.servlet.mvc.BaseCommandController.bindAndValidate(BaseCommandController.java:295)
 org.springframework.web.servlet.mvc.AbstractFormController.handleRequestInternal(AbstractFormController.java:236)
 org.springframework.web.servlet.mvc.AbstractController.handleRequest(AbstractController.java:128)
 org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter.handle(SimpleControllerHandlerAdapter.java:44)
 org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:522)
 org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:321)
 javax.servlet.http.HttpServlet.service(HttpServlet.java:802)
 com.blizzard.wow.web.filter.WelcomeFilter.doFilter(WelcomeFilter.java:52)
 org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:73)
 org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:73)</pre>