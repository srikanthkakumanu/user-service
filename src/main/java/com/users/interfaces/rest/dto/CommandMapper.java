package com.users.interfaces.rest.dto;

import com.users.application.CreateUser;
import com.users.application.RegisterUser;
import com.users.application.UpdateProfile;
import com.users.application.UpdateUser;
import com.users.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/** Turns request bodies into use-case commands. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommandMapper {

	RegisterUser.Command toCommand(Requests.Register request);

	CreateUser.Command toCommand(Requests.CreateUser request);

	UpdateUser.Command toCommand(UserId id, Requests.UpdateUser request);

	UpdateProfile.Command toCommand(UserId id, Requests.UpdateProfile request);
}
