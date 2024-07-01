# Filmorate app
## Database

### ER диаграмма
Диаграмма находится [здесь](https://dbdiagram.io/d/Filmorate-66718aaf5a764b3c72c8eb78)

![er](https://github.com/kostylevv/filmorate/assets/725305/6d3acd03-e480-4d03-9434-204240bcfb51)



#### Реализация "дружбы"
Решение с друзьями взято [отсюда](https://www.bitbytebit.xyz/i/131171653/option-improved-version-adding-rule-userid-userid) - см. Option #2

Оно не лишено недостатков но понравилось мне больше всего.

При реализации решения необходимо обеспечить программно, чтобы в каждой записи (строке) user_id_1 всегда был меньше user_id_2 чтобы избежать дублирующих записей.
Т.е.
```
// Пользователь с ID = 400 добавляет запрос на дружбу пользователю с ID = 10
INSERT INTO friends VALUES (10, 400, 'REQ_U2');
```

Поле status типа enum служит для сохранения информации о статусе дружбы:
- REQ_U1 - запрос на дружбу отправил пользователь с user_id_1 (1 для H2)
- REQ_U2 - запрос на дружбу отправил пользователь с user_id_2 (2 для H2)
- FRIENDS - взаимные друзья (3 для H2)
Мне кажется, что это поле - главная фича этого решения: в будущем функционал "взаимоотношений", если потребуется, может быть расширен по требованию.
Например, можно ввести значение BLOCKED (4 для H2), которое будет обозначать, что пользователи "раздружились".

### Примеры запросов

#### Получение списка "запросов на дружбу"
```
// Для пользователя с ID = 100
SELECT * FROM friends 
WHERE (user_id_1 = 100 AND status = 'REQ_U2')
OR (user_id_2 = 100 AND status = 'REQ_U1')
```

#### Получение списка друзей
```
// Для пользователя с ID = 100
SELECT * FROM friends 
WHERE (user_id_1 = 100 AND status = 'FRIENDS')
OR (user_id_2 = 100 AND status = 'FRIENDS')
```

#### Получение самых рейтинговых (залайканных) фильмов
```
// ТОП-50
SELECT * FROM films 
WHERE id IN (SELECT film_id
                      FROM likes
                      ORDER BY count(film_id) DESC
                      LIMIT 50);
```

#### Получение всех фильмов понравившихся пользователю
```
// пользователь с ID = 100
SELECT * FROM films 
WHERE id IN (SELECT film_id FROM likes
              WHERE user_id = 100);
```

#### Получение всех фильмов определенного рейтинга по MPA
```
// рейтинг NC-17
SELECT * FROM films 
WHERE rating_id = (SELECT id FROM rating
              WHERE title = 'NC-17');
```
