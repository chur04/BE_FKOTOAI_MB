package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.response.SubscriptionPackageResponse;
import com.g5.fokotoai.entity.SubscriptionPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionPackageMapper {

    @Mapping(target = "status", expression = "java(pkg.getStatus().name())")
    SubscriptionPackageResponse toResponse(SubscriptionPackage pkg);
}
