package de.seuhd.campuscoffee.tests.system;

import de.seuhd.campuscoffee.api.dtos.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static de.seuhd.campuscoffee.tests.SystemTestUtils.Requests.userRequests;
import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class UsersSystemTests extends AbstractSysTest {

    private List<UserDto> createSampleUsers() {
        List<UserDto> toCreate = List.of(
                UserDto.builder().loginName("maxmustermann").emailAddress("max.mustermann@campus.de").firstName("Max").lastName("Mustermann").build(),
                UserDto.builder().loginName("student2023").emailAddress("student2023@study.org").firstName("Student").lastName("Example").build()
        );
        return userRequests.create(toCreate);
    }

    @Test
    void createUser() {
        UserDto userToCreate = UserDto.builder()
                .loginName("jane_doe")
                .emailAddress("jane.doe@uni-heidelberg.de")
                .firstName("Jane")
                .lastName("Doe")
                .build();
        UserDto createdUser = userRequests.create(List.of(userToCreate)).getFirst();

        assertEqualsIgnoringIdAndTimestamps(createdUser, userToCreate);
    }

    @Test
    void getAllUsers_containsCreated() {
        List<UserDto> created = createSampleUsers();

        List<UserDto> allDtos = userRequests.retrieveAll();
        assertThat(allDtos).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsAll(created.stream()
                        .map(dto -> dto.toBuilder().id(null).createdAt(null).updatedAt(null).build())
                        .toList());
    }

    @Test
    void getUserById_returnsExpected() {
        List<UserDto> created = createSampleUsers();
        UserDto byId = userRequests.retrieveById(created.getFirst().id());
        assertEqualsIgnoringTimestamps(byId, created.getFirst());
    }

    @Test
    void getUserByLoginName_returnsExpected() {
        List<UserDto> created = createSampleUsers();
        UserDto byLoginName = userRequests.retrieveByFilter("loginName", created.getLast().loginName());
        assertEqualsIgnoringTimestamps(byLoginName, created.getLast());
    }

    @Test
    void updateUser_changesFirstName() {
        List<UserDto> created = createSampleUsers();
        UserDto updatedPayload = created.getFirst().toBuilder().firstName("UpdatedName").build();
        List<UserDto> updatedDtos = userRequests.update(List.of(updatedPayload));
        assertThat(updatedDtos.getFirst().firstName()).isEqualTo("UpdatedName");
    }

    @Test
    void deleteUser_returnsNoContent() {
        List<UserDto> created = createSampleUsers();
        int status = userRequests.deleteAndReturnStatusCodes(List.of(created.getLast().id())).getFirst();
        assertThat(status).isEqualTo(204);
    }

    @Test
    void createUser_validationError_returns400() {
        // invalid: space in loginName, invalid email, empty names
        UserDto invalid = UserDto.builder()
                .loginName("invalid name")
                .emailAddress("not-an-email")
                .firstName("")
                .lastName("")
                .build();

        given().contentType(JSON).body(invalid)
                .when().post("/api/users")
                .then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createUser_duplicateLoginName_returns409() {
        UserDto a = UserDto.builder().loginName("dupe_login").emailAddress("a@x.de").firstName("A").lastName("A").build();
        UserDto b = UserDto.builder().loginName("dupe_login").emailAddress("b@x.de").firstName("B").lastName("B").build();
        // first succeeds
        given().contentType(JSON).body(a).when().post("/api/users").then().statusCode(HttpStatus.CREATED.value());
        // duplicate loginName conflicts
        given().contentType(JSON).body(b).when().post("/api/users").then().statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void createUser_duplicateEmail_returns409() {
        UserDto a = UserDto.builder().loginName("unique_login1").emailAddress("dupe@x.de").firstName("A").lastName("A").build();
        UserDto b = UserDto.builder().loginName("unique_login2").emailAddress("dupe@x.de").firstName("B").lastName("B").build();
        given().contentType(JSON).body(a).when().post("/api/users").then().statusCode(HttpStatus.CREATED.value());
        given().contentType(JSON).body(b).when().post("/api/users").then().statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void getUserById_notFound_returns404() {
        given().contentType(JSON)
                .when().get("/api/users/{id}", 999999L)
                .then().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateUser_mismatchedIds_returns400() {
        // create valid user
        UserDto created = userRequests.create(List.of(
                UserDto.builder().loginName("mismatch_login").emailAddress("mismatch@x.de").firstName("A").lastName("A").build()
        )).getFirst();
        // send mismatched id in body
        UserDto payload = created.toBuilder().id(created.id() + 1).firstName("X").build();
        given().contentType(JSON).body(payload)
                .when().put("/api/users/{id}", created.id())
                .then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateUser_notFound_returns404() {
        // body id exists but does not exist in DB
        UserDto payload = UserDto.builder()
                .id(999999L)
                .loginName("ghost")
                .emailAddress("ghost@x.de")
                .firstName("Ghost")
                .lastName("User")
                .build();
        given().contentType(JSON).body(payload)
                .when().put("/api/users/{id}", 999999L)
                .then().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteUser_notFound_returns404() {
        given().when().delete("/api/users/{id}", 999999L)
                .then().statusCode(HttpStatus.NOT_FOUND.value());
    }
}