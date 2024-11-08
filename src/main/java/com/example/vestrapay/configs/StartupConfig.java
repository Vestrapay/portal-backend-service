package com.example.vestrapay.configs;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.superadmin.payment.model.PaymentMethods;
import com.example.vestrapay.superadmin.payment.repository.PaymentMethodRepository;
import com.example.vestrapay.merchant.roles_and_permissions.models.Permissions;
import com.example.vestrapay.merchant.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.merchant.roles_and_permissions.repository.PermissionRepository;
import com.example.vestrapay.merchant.roles_and_permissions.repository.RolePermissionRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StartupConfig implements ApplicationListener<ApplicationReadyEvent> {
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;
    private final PasswordEncoder passwordEncoder;
    private static final String VESTRAPAY  = "VESTRAPAY";
    @Value("${payment.methods}")
    private List<String>paymentMethods;


    private Mono<User> persistUser(){
        return userRepository.findUserByEmail(adminEmail)
                .flatMap(user -> {
                    log.warn("vestraPay super admin {} already exists",adminEmail);
                    return Mono.just(user);

                }).switchIfEmpty(Mono.defer(() -> {
                    log.info("about creating vestraPay super admin");
                    User adminUser = User.builder()
                            .uuid(UUID.randomUUID().toString())
                            .country("NIGERIA")
                            .firstName(VESTRAPAY)
                            .lastName(VESTRAPAY)
                            .email(adminEmail)
                            .phoneNumber(VESTRAPAY)
                            .merchantId(VESTRAPAY)
                            .businessName(VESTRAPAY)
                            .password(passwordEncoder.encode(adminPassword))
                            .userType(UserType.SUPER_ADMIN)
                            .enabled(true)
                            .kycCompleted(true)
                            .build();

                    return userRepository.save(adminUser);

                }));
    }

    private Mono<Void> persistPermissions(){
        Set<String> permissions = appPermissions();
        for (String permission:permissions){
            Optional<Permissions> permissions1 = permissionRepository.findByPermissionName(permission).blockOptional();
            if (permissions1.isPresent()){
                log.error("permission already exists for {}",permissions1.get().getPermissionName());

            }
            else {
                permissionRepository.save(Permissions.builder()
                                .uuid(UUID.randomUUID().toString())
                                .permissionName(permission)
                                .permissionDescription(permission)
                        .build()).block();
                log.info("{} created",permission);
            }
        }
        return Mono.empty();

    }



    private Mono<List<Permissions>> createAdminRolePermission(){
        return userRepository.findUserByEmail(adminEmail)
                .flatMap(user -> {
                    return permissionRepository.findAll().collectList().flatMap(Mono::just);
                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("vestraPay super admin {} already exists",adminEmail);
                    throw new CustomException();
                }));

    }


    private Set<String> appPermissions(){
        return Set.of("CREATE_USER",
                "UPDATE_USER",
                "DELETE_USER",
                "VIEW_USER",
                "CREATE_ADMIN",
                "VIEW_ADMINS",
                "DELETE_ADMIN",
                "UPDATE_ADMIN",
                "CREATE_ROLES",
                "VIEW_ALL_ROLES",
                "DELETE_ROLE",
                "CREATE_COMPLIANCE_ADMIN",
                "VIEW_ALL_TRANSACTIONS",
                "CREATE_SETTLEMENT_ACCOUNT",
                "UPDATE_SETTLEMENT_ACCOUNT",
                "VALIDATE_KYC",
                "UPDATE_ROLES",
                "GENERATE_KEYS",
                "VIEW_ALL_PERMISSIONS",
                "CREATE_MERCHANT_USER",
                "COMPLIANCE_PERMISSION"
                );
    }

    Mono<Void>persistDefaultPaymentMethods(){
        paymentMethods.forEach(s -> {
            Optional<PaymentMethods> paymentMethods1 = paymentMethodRepository.findByName(s).blockOptional();
            if (paymentMethods1.isEmpty()){
                log.info("creating payment method {}",s);
                paymentMethodRepository.save(PaymentMethods.builder()
                                .name(s)
                                .uuid(UUID.randomUUID().toString())
                        .build()).subscribe();
            }
            else {
                log.warn("payment method {} already exist",s);
            }

        });

        return Mono.empty();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("starting application startup configs");
        persistPermissions().subscribe();
        persistUser().flatMap(user ->createAdminRolePermission()
                .flatMap(permissions -> {
                    permissions.forEach(permissions1 -> {
                        rolePermissionRepository.findByUserIdAndPermissionId(user.getUuid(), permissions1.getPermissionName())
                                .flatMap(rolePermission -> {
                                    log.warn("role permission already exists. {}",rolePermission);
                                    return Mono.just(rolePermission);
                                }).switchIfEmpty(Mono.defer(() -> {
                                    RolePermission rolePermission = RolePermission.builder()
                                            .uuid(UUID.randomUUID().toString())
                                            .merchantID(user.getMerchantId())
                                            .permissionId(permissions1.getPermissionName())
                                            .userId(user.getUuid())
                                            .build();
                                    return rolePermissionRepository.save(rolePermission);
                                })).subscribe();
                    });
                    return Mono.empty();
                })).subscribe();
        persistDefaultPaymentMethods().subscribe();
    }
}
