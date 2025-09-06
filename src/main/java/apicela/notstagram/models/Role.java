package apicela.notstagram.models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    private Date expirationDate;

    public enum Values{
        BASIC(2L),
        ADMIN(1L);

        long Id;
        Values(long id){
            this.Id = id;
        }
    }
}