package com.example.vestrapay.roles_and_permissions.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("role_permissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RolePermission {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    @Column("user_id")
    private String userId;
    @Column("merchant_id")
    private String merchantID;
    @Column("permission_id")
    private String permissionId;

}
