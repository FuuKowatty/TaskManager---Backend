package pl.bartoszmech.feature;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import pl.bartoszmech.BaseIntegrationTest;
import pl.bartoszmech.domain.accountidentifier.dto.CreateUserRequestDto;

import pl.bartoszmech.domain.accountidentifier.dto.UpdateUserRequestDto;
import pl.bartoszmech.domain.accountidentifier.dto.UserDto;
import pl.bartoszmech.domain.task.dto.CreateTaskRequestDto;
import pl.bartoszmech.domain.task.dto.TaskDto;
import pl.bartoszmech.infrastructure.auth.dto.JwtResponseDto;
import pl.bartoszmech.infrastructure.auth.dto.TokenRequestDto;
import pl.bartoszmech.infrastructure.auth.dto.TokenResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.bartoszmech.domain.accountidentifier.UserRoles.ADMIN;
import static pl.bartoszmech.domain.accountidentifier.UserRoles.EMPLOYEE;
import static pl.bartoszmech.domain.accountidentifier.UserRoles.MANAGER;

public class ShouldAuthenticateManageTasksAndUsersIntegrationTest extends BaseIntegrationTest {
    @Test
    public void should_authenticate_and_manage_tasks_if_user_has_permission() throws Exception {
        //SECURITY
        //Step 1: An admin user can log in to their account.
        String adminEmail = "admin@gmail@gmail.com";
        String adminPassword = "zaq1@WSX";
        mockMvc.perform(post("/accounts/register")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateUserRequestDto.builder()
                                .firstName("Dany")
                                .lastName("Abramov")
                                .email(adminEmail)
                                .password(adminPassword)
                                .role(ADMIN)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String adminToken = objectMapper.readValue(mockMvc.perform(post("/accounts/token")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(TokenRequestDto.builder()
                                .username(adminEmail)
                                .password(adminPassword)
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), JwtResponseDto.class).token();


        //Step 2: A manager user can log in to their account.
        String managerEmail = "manager@gmail@gmail.com";
        String managerPassword = "zaq1@WSX";
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateUserRequestDto.builder()
                                .firstName("Dany")
                                .lastName("Abramov")
                                .email(managerEmail)
                                .password(managerPassword)
                                .role(MANAGER)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String managerToken = objectMapper.readValue(mockMvc.perform(post("/accounts/token")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(TokenRequestDto.builder()
                                .username(managerEmail)
                                .password(managerPassword)
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), JwtResponseDto.class).token();


        //Step 3: An employee user can log in to their account.
        String employeeEmail = "employee@gmail@gmail.com";
        String employeePassword = "zaq1@WSX";
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateUserRequestDto.builder()
                                .firstName("Dany")
                                .lastName("Abramov")
                                .email(employeeEmail)
                                .password(employeePassword)
                                .role(EMPLOYEE)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        TokenResponseDto employee = objectMapper.readValue(mockMvc.perform(post("/accounts/token")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(TokenRequestDto.builder()
                                .username(employeeEmail)
                                .password(employeePassword)
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), TokenResponseDto.class);
        String employeeToken = employee.token();


        //Step 4: A user with incorrect email cannot log in.
        mockMvc.perform(post("/accounts/token")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(TokenRequestDto.builder()
                                .username("NonExistingEmail@gmail.com")
                                .password("zaq1@WSX")
                                .build())))
                .andExpect(status().isUnauthorized());


        //TASK DOMAIN
        //Step 5: Admin can create a new task.
        String createdTaskByAdminResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateTaskRequestDto.builder()
                                .title("created title by admin")
                                .description("created description by admin")
                                .endDate(LocalDateTime.now(adjustableClock).plusDays(1))
                                .assignedTo(employee.id())
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TaskDto createdTaskByAdmin = objectMapper.readValue(createdTaskByAdminResponse, TaskDto.class);


        //Step 6: Manager can create a new task.
        String createdTaskByManagerResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateTaskRequestDto.builder()
                                .title("created title by manager")
                                .description("created description by manager")
                                .endDate(LocalDateTime.now(adjustableClock).plusDays(1))
                                .assignedTo(employee.id())
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TaskDto createdTaskByManager = objectMapper.readValue(createdTaskByManagerResponse, TaskDto.class);


        //Step 7: Employee cannot create a new task.
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateTaskRequestDto.builder()
                                .title("tasktitle3")
                                .description("taskdescription")
                                .endDate(LocalDateTime.now(adjustableClock).plusDays(1))
                                .assignedTo(employee.id())
                                .build())))
                .andExpect(status().isForbidden());


        //Step 8: Employee cannot edit an existing task.
        mockMvc.perform(put("/api/tasks/" + createdTaskByAdmin.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateTaskRequestDto.builder()
                                .title("qtt3t3te")
                                .description("tt3t3t3ff3")
                                .endDate(LocalDateTime.now(adjustableClock).plusDays(2))
                                .assignedTo(createdTaskByManager.assignedTo())
                                .build())))
                .andExpect(status().isForbidden());


        //Step 9: Manager can delete an existing task.
        mockMvc.perform(delete("/api/tasks/" + createdTaskByAdmin.id())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        //Step 10: Employee cannot delete an existing task.


        mockMvc.perform(delete("/api/tasks/" + createdTaskByManager.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 11: Admin can view a list of tasks.
        String adminListTasksResponse = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<TaskDto> adminListTask = objectMapper.readValue(adminListTasksResponse, new TypeReference<>() {
        });


        //Step 12: Manager can view a list of tasks.
        String managerListTasksResponse = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<TaskDto> managerListTask = objectMapper.readValue(managerListTasksResponse, new TypeReference<>() {
        });

        assertThat(adminListTask).isNotEmpty();
        assertThat(adminListTask).isEqualTo(managerListTask);


        //Step 13 Employee cannot view a list of tasks
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 14: Authenticated can view a list of employee tasks.
        mockMvc.perform(get("/api/tasks/employee/" + employee.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());


        //Step 15 Admin can see task by id
        mockMvc.perform(get("/api/tasks/" + createdTaskByManager.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());


        //Step 16 Employee can see his task by id
        mockMvc.perform(get("/api/tasks/" + createdTaskByManager.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        //Step 17 Manager edit task assignedTo and employee do not get task because it is not assigned to him
        String editedTaskByManagerResponse = mockMvc.perform(put("/api/tasks/" + createdTaskByManager.id())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateTaskRequestDto.builder()
                                .title("edited title by manager")
                                .description("I was updated by manager")
                                .endDate(LocalDateTime.now(adjustableClock).plusDays(2))
                                .assignedTo(employee.id() + 1)
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TaskDto editedTaskByManager = objectMapper.readValue(editedTaskByManagerResponse, TaskDto.class);

        mockMvc.perform(get("/api/tasks/" + editedTaskByManager.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 18 Manager can view task with updated id
        mockMvc.perform(get("/api/tasks/" + editedTaskByManager.id())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());


        //Step 19: Admin cannot mark a task as completed.
        mockMvc.perform(patch("/api/tasks/" + editedTaskByManager.id() + "/complete")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 20: Manager cannot mark a task as completed.
        mockMvc.perform(patch("/api/tasks/" + editedTaskByManager.id() + "/complete")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 21: Employee cannot mark task as completed because it is not assigned to him.
        mockMvc.perform(patch("/api/tasks/" + editedTaskByManager.id() + "/complete")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 22: Admin can edit an existing task.
        String updatedTaskByAdminResponse = mockMvc.perform(put("/api/tasks/" + editedTaskByManager.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CreateTaskRequestDto.builder()
                                .title("edited title by admin")
                                .description("I was updated by admin")
                                .endDate(LocalDateTime.now(adjustableClock).plusDays(2))
                                .assignedTo(employee.id())
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TaskDto updatedTaskByAdmin = objectMapper.readValue(updatedTaskByAdminResponse, TaskDto.class);


        // Step 23 Employee can mark as complete his task
        mockMvc.perform(patch("/api/tasks/" + updatedTaskByAdmin.id() + "/complete")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());


        //Step 24 Admin can list all tasks and receive list with size 1
        String listTasksResponse = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<TaskDto> listTask = objectMapper.readValue(listTasksResponse, new TypeReference<>() {
        });
        assertThat(listTask.size()).isEqualTo(1);


        //Step 25: Admin can delete an existing task.
        String deletedTaskResponse = mockMvc.perform(delete("/api/tasks/" + updatedTaskByAdmin.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TaskDto deletedTask = objectMapper.readValue(deletedTaskResponse, new TypeReference<>() {
        });

        assertThat(deletedTask).isEqualTo(listTask.get(0));
        assertThat(deletedTask.isCompleted()).isTrue();

        //USER DOMAIN
        //Step 26: Employee cannot add a new user.
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 27: Manager cannot add a new user.
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 28: Employee cannot findById an existing user.
        mockMvc.perform(get("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 29: Manager cannot findById an existing user.
        mockMvc.perform(get("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 30: Admin can findById an existing user.
        mockMvc.perform(get("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());


        //Step 31: Employee cannot read all users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 32: Manager cannot read all users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 33: Admin can read all user.
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());


        //Step 34: Employee cannot edit an existing user.
        mockMvc.perform(put("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 35: Manager cannot edit an existing user.
        mockMvc.perform(put("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 36: Employee cannot delete an existing user.
        mockMvc.perform(delete("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 37: Admin can edit an existing user.
        String updatedUserResponse = mockMvc.perform(put("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(UpdateUserRequestDto.builder()
                                .firstName("Danny")
                                .lastName("Daniels")
                                .email("abc@gmail.com")
                                .password("password")
                                .role(MANAGER)
                                .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UserDto updatedUser = objectMapper.readValue(updatedUserResponse, UserDto.class);


        //Step 38: Manager cannot delete an existing user.
        mockMvc.perform(delete("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());


        //Step 39: Admin can delete an existing user.
        String deletedUserResponse = mockMvc.perform(delete("/api/users/" + employee.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UserDto deletedUser = objectMapper.readValue(deletedUserResponse, UserDto.class);
        assertThat(deletedUser).isEqualTo(updatedUser);
    }
}