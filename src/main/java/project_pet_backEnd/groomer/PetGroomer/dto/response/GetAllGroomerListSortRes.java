package project_pet_backEnd.groomer.PetGroomer.dto.response;

import lombok.Data;

import java.sql.Date;

@Data
public class GetAllGroomerListSortRes {
    private Integer pgId;
    private Integer manId;
    private String pgName;
    private String pgGender;//String 男性 / 女性
    private String pgPic;//Base64
    private String pgEmail;
    private String pgPh;
    private String pgAddress;
    private Date pgBirthday;//sql.date
    private Integer NumAppointments;
    // 此處省略建構子、Getter 和 Setter 方法
}