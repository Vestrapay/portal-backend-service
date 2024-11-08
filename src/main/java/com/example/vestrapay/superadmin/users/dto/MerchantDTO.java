package com.example.vestrapay.superadmin.users.dto;

import com.example.vestrapay.merchant.business.models.Business;
import com.example.vestrapay.merchant.roles_and_permissions.models.Permissions;
import com.example.vestrapay.merchant.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.merchant.transactions.models.Transaction;
import com.example.vestrapay.merchant.users.models.User;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class MerchantDTO {
    private User merchant;
    private List<User> merchantUsers;
    private Business merchantBusiness;
    private List<RolePermission> merchantPermissions;
    private List<Transaction> merchantTransaction;
}
