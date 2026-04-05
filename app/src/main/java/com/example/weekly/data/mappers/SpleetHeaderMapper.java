package com.example.weekly.data.mappers;

import com.example.weekly.data.local.entities.SpleetHeaderEntity;
import com.example.weekly.domain.SpleetHeader;

public class SpleetHeaderMapper {
    public static SpleetHeaderEntity toEntity(SpleetHeader domain) {
        if (domain == null) return null;
        SpleetHeaderEntity entity = new SpleetHeaderEntity();
        entity.id = domain.id;
        entity.name = domain.name;
        return entity;
    }

    public static SpleetHeader toDomain(SpleetHeaderEntity entity) {
        if (entity == null) return null;
        return new SpleetHeader(entity.id, entity.name);
    }
}
