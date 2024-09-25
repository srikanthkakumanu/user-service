package user.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import user.domain.Address;
import user.domain.UserProfile;
import user.dto.AddressDTO;
import user.dto.UserProfileDTO;
import user.exception.ExceptionInfo;
import user.exception.UserServiceException;
import user.mapper.AddressMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class CommonUtil {

    public static final BiPredicate<String, String> passwordMatch = String::equals;

    public static final BiFunction<AddressDTO, Address, Boolean> bothAddressesExist =
            (dto, domain) -> Objects.nonNull(dto) && Objects.nonNull(domain);

    public static final AddressMergeFunc<AddressDTO, Address, AddressMapper, Address> addressMerger =
            (dto, domain, mapper, result) -> mapper.copyTo(dto, domain);

    public static void mergeIfBothExist(AddressDTO dtoAddr, Address existingAddr, BiConsumer<AddressDTO, Address> mergeAction) {
        if (bothAddressesExist.apply(dtoAddr, existingAddr)) {
            mergeAction.accept(dtoAddr, existingAddr);
        }
    }

    public static UserProfile mergeAddresses(UserProfileDTO dto, UserProfile existingProfile, AddressMapper mapper) {

        mergeIfBothExist(dto.getCurrentAddress(), existingProfile.getCurrentAddress(),
                (currentAddr, existingAddr) -> existingProfile.setCurrentAddress(
                        addressMerger.apply(currentAddr, existingAddr, mapper, existingAddr)
                )
        );

        mergeIfBothExist(dto.getPermanentAddress(), existingProfile.getPermanentAddress(),
                (permAddr, existingAddr) -> existingProfile.setPermanentAddress(
                        addressMerger.apply(permAddr, existingAddr, mapper, existingAddr)
                )
        );
        return existingProfile;
    }

    public static Address mergeAddresses(AddressDTO dto, Address foundAddress, AddressMapper mapper) {

        mergeIfBothExist(dto, foundAddress,
                (currentAddr, existingAddr)
                        -> addressMerger.apply(currentAddr, existingAddr, mapper, existingAddr));
        return foundAddress;
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    public static Timestamp toSQLTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime.toString());
    }

    public static ResponseEntity<Object> buildUserErrorResponse(Exception re, WebRequest request) {

        Map<String, List<Object>> body = new HashMap<>();
        List<Object> errors = new ArrayList<>();

        ExceptionInfo info = switch (re) {
            case UserServiceException use ->
                buildExceptionInfo(use.getEntityName(), use.getStatus().value(),
                        use.getStatus(), use.getMessage(), ZonedDateTime.now(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
            default ->
                    buildExceptionInfo("user", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected User Service Error",
                            ZonedDateTime.now(), ((ServletWebRequest) request).getRequest().getRequestURI());
        };
        errors.add(info);
        body.put("errors", errors);
        return new ResponseEntity<>(body, info.status());
    }

    public static ExceptionInfo buildExceptionInfo(String entityName,
                                                   int code,
                                                   HttpStatus status,
                                                   String message,
                                                   ZonedDateTime timestamp, String path) {
        return new ExceptionInfo(UUID.randomUUID().toString(), entityName, code, status, message, ZonedDateTime.now(), path);
    }
}
