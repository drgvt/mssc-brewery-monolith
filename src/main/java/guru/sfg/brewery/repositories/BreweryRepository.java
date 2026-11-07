/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * репозиторий для работы с пивоварнями в базе данных, который благодаря
 * наследованию от JpaRepository сразу получает готовые методы для сохранения,
 * поиска и удаления сущностей Brewery без написания SQL-кода
 *
 * В domain лежит сама сущность (класс Brewery с полями и @Entity), а в repositories —
 * интерфейс (BreweryRepository), который управляет сохранением и поиском этой сущности в БД.
 */

package guru.sfg.brewery.repositories;

import guru.sfg.brewery.domain.Brewery;

import guru.sfg.brewery.domain.Brewery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Created by jt on 2019-01-26.
 */
public interface BreweryRepository extends JpaRepository<Brewery, UUID> {
}
