package de.seuhd.campuscoffee.tests.system;

import de.seuhd.campuscoffee.api.dtos.UserDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.seuhd.campuscoffee.tests.SystemTestUtils.Requests.userRequests;
import static org.assertj.core.api.Assertions.assertThat;

public class UsersSystemTests extends AbstractSysTest {

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
    void getAllAndFilterAndGetByIdAndUpdateAndDelete() {
        // create two users
        List<UserDto> toCreate = List.of(
                UserDto.builder().loginName("maxmustermann").emailAddress("max.mustermann@campus.de").firstName("Max").lastName("Mustermann").build(),
                UserDto.builder().loginName("student2023").emailAddress("student2023@study.org").firstName("Student").lastName("Example").build()
        );
        List<UserDto> created = userRequests.create(toCreate);

        // get all
        List<UserDto> allDtos = userRequests.retrieveAll();
        assertThat(allDtos).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsAll(toCreate);

        // get by id
        UserDto byId = userRequests.retrieveById(created.getFirst().id());
        assertEqualsIgnoringTimestamps(byId, created.getFirst());

        // filter by loginName
        UserDto byLoginName = userRequests.retrieveByFilter("loginName", created.getLast().loginName());
        assertEqualsIgnoringTimestamps(byLoginName, created.getLast());

        // update first name for first user
        UserDto updatedPayload = created.getFirst().toBuilder().firstName("UpdatedName").build();
        List<UserDto> updatedDtos = userRequests.update(List.of(updatedPayload));
        assertThat(updatedDtos.getFirst().firstName()).isEqualTo("UpdatedName");

        // delete second user
        int status = userRequests.deleteAndReturnStatusCodes(List.of(created.getLast().id())).getFirst();
        assertThat(status).isEqualTo(204);
    }

}