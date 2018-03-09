---
layout: post
title: Laravel 'Class log does not exist' RESOLVED
categories: laravel
year: 2016
---

In order to track down this issue, I followed advice that I saw at https://laracasts.com/discuss/channels/laravel/error-when-upgrading-to-52-class-log-does-not-exist.

I edited vendor/laravel/framework/src/Illuminate/Foundation/Bootstrap/LoadConfiguration.php as follows:

```
  protected function loadConfigurationFiles(Application $app, RepositoryContract $repository)
  {
    foreach ($this->getConfigurationFiles($app) as $key => $path) {
      var_dump('file: ' . $key . ' -- path: ' . $path);
    }
    foreach ($this->getConfigurationFiles($app) as $key => $path) {
      $repository->set($key, require $path);
      var_dump('loaded key: ' . $key . ' -- path: ' . $path);
    }
  }
```

Then I ran 'php artisan' which resulted in:

```
string(69) "file: filesystems -- path: /home/forge/default/config/filesystems.php"
string(71) "file: broadcasting -- path: /home/forge/default/config/broadcasting.php"
string(63) "file: services -- path: /home/forge/default/config/services.php"
string(57) "file: cache -- path: /home/forge/default/config/cache.php"
string(53) "file: app -- path: /home/forge/default/config/app.php"
string(61) "file: compile -- path: /home/forge/default/config/compile.php"
string(63) "file: database -- path: /home/forge/default/config/database.php"
string(55) "file: auth -- path: /home/forge/default/config/auth.php"
string(89) "file: laravel-menu.settings -- path: /home/forge/default/config/laravel-menu/settings.php"
string(83) "file: laravel-menu.views -- path: /home/forge/default/config/laravel-menu/views.php"
string(61) "file: session -- path: /home/forge/default/config/session.php"
string(55) "file: view -- path: /home/forge/default/config/view.php"
string(61) "file: entrust -- path: /home/forge/default/config/entrust.php"
string(55) "file: mail -- path: /home/forge/default/config/mail.php"
string(57) "file: queue -- path: /home/forge/default/config/queue.php"
string(75) "loaded key: filesystems -- path: /home/forge/default/config/filesystems.php"
string(77) "loaded key: broadcasting -- path: /home/forge/default/config/broadcasting.php"
string(69) "loaded key: services -- path: /home/forge/default/config/services.php"
string(63) "loaded key: cache -- path: /home/forge/default/config/cache.php"
```

You can see that Laravel wants to load 15 files but only four were loaded correctly. Therefore, the fifth file (app.php) has some kind of error.

In my particular case, the ending comma was missing from one of the lines.
