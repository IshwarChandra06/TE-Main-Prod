package com.eikona.tech.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eikona.tech.entity.Privilege;
import com.eikona.tech.entity.Role;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.PrivilegeRepository;
import com.eikona.tech.repository.RoleRepository;
import com.eikona.tech.repository.UserRepository;

@Service
public class DBSeeder implements CommandLineRunner {

	private UserRepository userRepository;

	private PrivilegeRepository privilegeRepository;

	private RoleRepository roleRepository;

	private PasswordEncoder passwordEncoder;

	public DBSeeder(PrivilegeRepository privilegeRepository, RoleRepository roleRepository,
			UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.privilegeRepository = privilegeRepository;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		List<Privilege> privilegeList = privilegeRepository.findAllByIsDeletedFalse();

		if (null == privilegeList || privilegeList.isEmpty()) {
			List<Privilege> privileges = SeedPrivileges();

			Role admin = seedRole(privileges);

			seedUser(admin,"Admin");
			seedUser(admin,"BISAdmin");

		}

	}

	private List<Privilege> SeedPrivileges() {
		Privilege userView = new Privilege("user_view", false);
		Privilege userCreate = new Privilege("user_create", false);
		Privilege userUpdate = new Privilege("user_update", false);
		Privilege userDelete = new Privilege("user_delete", false);
		
		Privilege roleView = new Privilege("role_view", false);
		Privilege roleCreate = new Privilege("role_create", false);
		Privilege roleUpdate = new Privilege("role_update", false);
		Privilege roleDelete = new Privilege("role_delete", false);
		
		Privilege accessLevelView = new Privilege("access_level_view", false);
		Privilege accessLevelUpdate = new Privilege("access_level_update", false);
		Privilege accessLevelDelete = new Privilege("access_level_delete", false);
		Privilege accessLevelExport= new Privilege("access_level_export", false);
		
		Privilege blacklistView = new Privilege("blacklist_view", false);
		Privilege blacklistUpdate = new Privilege("blacklist_update", false);
		Privilege blacklistDelete = new Privilege("blacklist_delete", false);
		Privilege blacklistCreate = new Privilege("blacklist_create", false);
		Privilege blacklistExport = new Privilege("blacklist_export", false);
		Privilege suspensionView = new Privilege("suspension_view", false);
		Privilege suspensionExport = new Privilege("suspension_export", false);
		
		Privilege cardView = new Privilege("card_tracking_view", false);
		Privilege cardCreate = new Privilege("card_tracking_create", false);
		Privilege cardUpdate = new Privilege("card_tracking_update", false);
		Privilege cardExport = new Privilege("card_tracking_export", false);
		
		Privilege lanyardView = new Privilege("lanyard_view", false);
		Privilege lanyardCreate = new Privilege("lanyard_create", false);
		Privilege lanyardUpdate = new Privilege("lanyard_update", false);
		Privilege lanyardExport = new Privilege("lanyard_export", false);
		
		Privilege employeeTypeView = new Privilege("employee_type_view", false);
		Privilege employeeTypeCreate = new Privilege("employee_type_create", false);
		Privilege employeeTypeUpdate = new Privilege("employee_type_update", false);
		Privilege employeeTypeDelete = new Privilege("employee_type_delete", false);
		
		Privilege privilegeView = new Privilege("privilege_view", false);
		Privilege privilegeUpdate = new Privilege("privilege_update", false);
		Privilege privilegeDelete = new Privilege("privilege_delete", false);
		
		Privilege employeeView = new Privilege("employee_view", false);
		Privilege employeeCreate = new Privilege("employee_create", false);
		Privilege employeeUpdate = new Privilege("employee_update", false);
		Privilege employeeDelete = new Privilege("employee_delete", false);
		Privilege employeeImport = new Privilege("employee_import", false);
		Privilege employeeExport = new Privilege("employee_export", false);
		
		Privilege employeeRosterView = new Privilege("employee_roster_view", false);
		Privilege employeeRosterExport = new Privilege("employee_roster_export", false);
		Privilege deviceView = new Privilege("device_view", false);
		Privilege deviceUpdate = new Privilege("device_update", false);
		Privilege deviceDelete = new Privilege("device_delete", false);
		Privilege deviceExport = new Privilege("device_export", false);
		
		Privilege deviceHealthStatus = new Privilege("device_health_status", false);
		
		Privilege deviceDashboardView = new Privilege("device_dashboard_view", false);
		
		Privilege emailLogView = new Privilege("email_log_view", false);
		Privilege emailLogExport = new Privilege("email_log_export", false);
		
		Privilege buildingView = new Privilege("building_view", false);
		Privilege buildingCreate = new Privilege("building_create", false);
		Privilege buildingUpdate = new Privilege("building_update", false);
		Privilege buildingDelete = new Privilege("building_delete", false);
		
		Privilege plantView = new Privilege("plant_view", false);
		Privilege plantCreate = new Privilege("plant_create", false);
		Privilege plantUpdate = new Privilege("plant_update", false);
		Privilege plantDelete = new Privilege("plant_delete", false);
		
		Privilege lostCardView = new Privilege("lost_card_tracking_view", false);
		Privilege lostCardExport = new Privilege("lost_card_tracking_export", false);
		
		Privilege damageCardView = new Privilege("damage_card_tracking_view", false);
		Privilege damageCardExport = new Privilege("damage_card_tracking_export", false);
		
		Privilege workStatusReportView = new Privilege("work_status_report_view", false);
		Privilege workStatusReportExport = new Privilege("work_status_report_export", false);
		
		Privilege activeEmployeeExport = new Privilege("active_employee_report_export", false);
		Privilege activeEmployeeView = new Privilege("active_employee_report_view", false);
		
		Privilege inactiveEmployeeView = new Privilege("inactive_employee_report_view", false);
		Privilege inactiveEmployeeExport = new Privilege("inactive_employee_report_export", false);
		
		Privilege accessLogView = new Privilege("access_log_view", false);
		Privilege accessLogExport = new Privilege("access_log_export", false);
		
		Privilege lanyardTypeView = new Privilege("lanyard_type_view", false);
		Privilege lanyardTypeCreate = new Privilege("lanyard_type_create", false);
		Privilege lanyardTypeUpdate = new Privilege("lanyard_type_update", false);
		Privilege lanyardTypeDelete = new Privilege("lanyard_type_delete", false);
		
		Privilege zoneView = new Privilege("zone_view", false);
		Privilege zoneCreate = new Privilege("zone_create", false);
		Privilege zoneUpdate = new Privilege("zone_update", false);
		Privilege zoneDelete = new Privilege("zone_delete", false);
		
		Privilege metalExceptionView = new Privilege("metal_exception_view", false);
		Privilege metalExceptionCreate = new Privilege("metal_exception_create", false);
		Privilege metalExceptionUpdate = new Privilege("metal_exception_update", false);
		Privilege metalExceptionDelete = new Privilege("metal_exception_delete", false);
		
		Privilege employeeAccessLevelReportView = new Privilege("employee_access_level_report_view", false);
		Privilege employeeAccessLevelReportExport = new Privilege("employee_access_level_report_export", false);

		List<Privilege> privileges = Arrays.asList(
				userView, userCreate, userUpdate, userDelete,roleView, roleCreate, roleUpdate, roleDelete,privilegeView,privilegeUpdate,privilegeDelete,
				employeeView, employeeCreate, employeeUpdate, employeeDelete,employeeImport,employeeExport,employeeRosterView,employeeRosterExport,deviceView,deviceUpdate,deviceDelete,deviceExport,
				lanyardTypeView,lanyardTypeCreate,lanyardTypeUpdate,lanyardTypeDelete,zoneView,zoneCreate,zoneUpdate,zoneDelete,plantView,plantCreate,plantUpdate,plantUpdate,plantDelete,
				buildingView,buildingCreate,buildingUpdate,buildingDelete,employeeTypeView,employeeTypeCreate,employeeTypeUpdate,employeeTypeDelete,
				metalExceptionUpdate,metalExceptionDelete,metalExceptionView,metalExceptionCreate,accessLevelView,accessLevelUpdate,accessLevelDelete,accessLevelExport,
				activeEmployeeExport,activeEmployeeView,emailLogView,emailLogExport,workStatusReportView,workStatusReportExport,deviceHealthStatus,
				lostCardView,lostCardExport,damageCardView,damageCardExport,inactiveEmployeeView,inactiveEmployeeExport,cardView,cardCreate,cardUpdate,cardExport,deviceDashboardView,
				accessLogView,accessLogExport,blacklistView,blacklistUpdate,blacklistDelete,blacklistCreate,blacklistExport,suspensionView,suspensionExport,
				lanyardView,lanyardCreate,lanyardUpdate,lanyardExport,employeeAccessLevelReportView,employeeAccessLevelReportExport
				);

		privilegeRepository.saveAll(privileges);
		return privileges;
	}

	private Role seedRole(List<Privilege> privileges) {
		Role admin = roleRepository.findByName("Admin");
		if (null == admin) {
			admin = new Role("Admin", privileges, false);
			roleRepository.save(admin);
		}
		return admin;
	}

	private void seedUser(Role admin,String username) {
		List<User> userList = userRepository.findAllByIsDeletedFalse();
		if (null == userList || userList.isEmpty()) {
			User adminUser = new User(username, passwordEncoder.encode("Admin@123"), true, admin, false);
			userRepository.save(adminUser);
		}
	}
	
	
}
