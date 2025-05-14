package com.zipline.service.excel;

import java.time.Duration;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zipline.api.GeoCodeResultVO;
import com.zipline.api.KakaoGeoClient;
import com.zipline.api.KakaoGeoClientAsync;
import com.zipline.api.KakaoGeocodeResponseDTO;
import com.zipline.entity.agentProperty.AgentProperty;
import com.zipline.entity.customer.Customer;
import com.zipline.entity.enums.PropertyCategory;
import com.zipline.entity.enums.PropertyType;
import com.zipline.entity.user.User;
import com.zipline.excel.AgentPropertyExcelDTO;
import com.zipline.excel.AgentPropertyExcelRowMapper;
import com.zipline.excel.CustomerExcelDTO;
import com.zipline.excel.CustomerExcelRowMapper;
import com.zipline.excel.ExcelReader;
import com.zipline.global.exception.excel.ExcelException;
import com.zipline.global.exception.excel.errorcode.ExcelErrorCode;
import com.zipline.global.exception.user.UserException;
import com.zipline.global.exception.user.errorcode.UserErrorCode;
import com.zipline.repository.agentProperty.AgentPropertyRepository;
import com.zipline.repository.customer.CustomerRepository;
import com.zipline.repository.user.UserRepository;

import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExcelServiceImpl implements ExcelService {

	private final ExcelReader excelReader;
	private final CustomerExcelRowMapper customerExcelRowMapper;
	private final AgentPropertyExcelRowMapper agentPropertyExcelRowMapper;
	private final KakaoGeoClient kakaoGeoClient;
	private final KakaoGeoClientAsync kakaoGeoAsyncClient;
	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final AgentPropertyRepository agentPropertyRepository;

	@Override
	@Transactional
	public Map<String, Integer> registerCustomerByExcel(MultipartFile excelFile, Long userUid) {
		List<CustomerExcelDTO> dtoList = readCustomerExcel(excelFile);
		log.info("[고객 엑셀 등록 시작] userUid={}, filename={}", userUid, excelFile.getOriginalFilename());
		checkCustomerDuplicateInExcel(dtoList);
		User user = getUserOrThrow(userUid);
		checkCustomerDuplicateInDB(dtoList);
		// List<Customer> customers = mapToCustomerEntities(dtoList, user);
		List<Customer> customers = mapToCustomerEntitiesAsync(dtoList, user);
		customerRepository.saveAll(customers);
		log.info("[고객 엑셀 등록 완료] 저장된 고객 수={}", customers.size());
		return Collections.singletonMap("success Count", customers.size());
	}

	@Override
	@Transactional
	public Map<String, Integer> registerPropertiesByExcel(MultipartFile excelFile, Long userUid) {
		long startTime = System.currentTimeMillis();
		List<AgentPropertyExcelDTO> dtoList = readPropertyExcel(excelFile);
		log.info("[매물 엑셀 등록 시작] userUid={}, filename={}", userUid, excelFile.getOriginalFilename());
		User user = getUserOrThrow(userUid);
		Map<String, Customer> customerMap = prepareCustomersForProperty(dtoList, user);
		// List<AgentProperty> properties = mapToAgentProperties(dtoList, customerMap, user);
		List<AgentProperty> properties = mapToAgentPropertiesAsync(dtoList, customerMap, user);
		agentPropertyRepository.saveAll(properties);

		long endTime = System.currentTimeMillis();
		Duration duration = Duration.ofMillis(endTime - startTime);
		long seconds = duration.toSecondsPart();
		long millis = duration.toMillisPart();
		log.info("[매물 엑셀 등록 완료] 저장된 매물 수={}, 소요 시간={}초 {}ms", properties.size(), seconds, millis);
		return Collections.singletonMap("success Count", properties.size());
	}

	private List<CustomerExcelDTO> readCustomerExcel(MultipartFile file) {
		return excelReader.readExcel(file, customerExcelRowMapper);
	}

	private List<AgentPropertyExcelDTO> readPropertyExcel(MultipartFile file) {
		return excelReader.readExcel(file, agentPropertyExcelRowMapper);
	}

	private void checkCustomerDuplicateInExcel(List<CustomerExcelDTO> list) {
		Set<String> uniqueKeySet = new HashSet<>();
		for (CustomerExcelDTO dto : list) {
			String key = dto.getName() + "," + dto.getPhoneNo();
			if (!uniqueKeySet.add(key)) {
				log.info("[엑셀 중복 고객] rowNum={}, key={}", dto.getRowNum(), key);
				throw new ExcelException(ExcelErrorCode.DUPLICATED_CUSTOMER, dto.getRowNum(), "name/phoneNo", key,
					"엑셀에 중복된 고객이 존재합니다.");
			}
		}
	}

	private void checkCustomerDuplicateInDB(List<CustomerExcelDTO> list) {
		Set<String> keySet = list.stream()
			.map(dto -> dto.getName() + "," + dto.getPhoneNo())
			.collect(Collectors.toSet());
		List<Customer> exists = customerRepository.findByNameAndPhoneNoPairs(keySet);
		if (!exists.isEmpty()) {
			Map<String, Integer> rowNumMap = list.stream()
				.collect(Collectors.toMap(dto -> dto.getName() + "," + dto.getPhoneNo(), CustomerExcelDTO::getRowNum));
			throw new ExcelException(ExcelErrorCode.DUPLICATED_CUSTOMER,
				convertToDuplicatedCustomerDetails(exists, rowNumMap));
		}
	}

	private List<Customer> mapToCustomerEntities(List<CustomerExcelDTO> list, User user) {
		log.info("[고객 매핑 시작] 대상 수={}", list.size());
		return list.stream().map(dto -> {
			String legalCode = resolveLegalDistrictCode(dto);
			return toCustomerEntity(dto, legalCode, user);
		}).toList();
	}

	private String resolveLegalDistrictCode(CustomerExcelDTO dto) {
		if (dto.getPreferredRegion() == null)
			return null;
		GeoCodeResultVO geo = getGeoCode(dto.getRowNum(), dto.getPreferredRegion());
		return geo.getLegalDistrictCode();
	}

	private Customer toCustomerEntity(CustomerExcelDTO dto, String legalCode, User user) {
		return Customer.builder()
			.user(user)
			.name(dto.getName())
			.phoneNo(dto.getPhoneNo())
			.telProvider(dto.getTelProvider())
			.legalDistrictCode(legalCode)
			.minRent(dto.getMinRent()).maxRent(dto.getMaxRent())
			.minDeposit(dto.getMinDeposit()).maxDeposit(dto.getMaxDeposit())
			.minPrice(dto.getMinPrice()).maxPrice(dto.getMaxPrice())
			.isBuyer(dto.isBuyer()).isSeller(dto.isSeller())
			.isLandlord(dto.isLandlord()).isTenant(dto.isTenant())
			.birthday(dto.getBirthDay())
			.build();
	}

	private Map<String, Customer> prepareCustomersForProperty(List<AgentPropertyExcelDTO> list, User user) {
		log.info("[매물 고객 준비 시작] 총 고객 수={}", list.size());
		Set<String> keys = extractCustomerKeys(list);
		Map<String, Customer> dbCustomers = findExistingCustomers(keys);
		log.info("[기존 고객 수] = {}", dbCustomers.size());
		List<Customer> newCustomers = extractNewCustomers(list, dbCustomers.keySet(), user);
		log.info("[신규 고객 수] = {}", newCustomers.size());
		customerRepository.saveAll(newCustomers);
		newCustomers.forEach(c -> dbCustomers.put(c.getName() + "," + c.getPhoneNo(), c));
		log.info("[고객 준비 완료] 전체 고객 수 = {}", dbCustomers.size());
		return dbCustomers;
	}

	private Set<String> extractCustomerKeys(List<AgentPropertyExcelDTO> list) {
		return list.stream()
			.map(dto -> dto.getCustomerName() + "," + dto.getPhoneNo())
			.collect(Collectors.toSet());
	}

	private Map<String, Customer> findExistingCustomers(Set<String> keys) {
		return customerRepository.findByNameAndPhoneNoPairs(keys).stream()
			.collect(Collectors.toMap(c -> c.getName() + "," + c.getPhoneNo(), Function.identity()));
	}

	private List<Customer> extractNewCustomers(List<AgentPropertyExcelDTO> list, Set<String> existingKeys, User user) {
		return list.stream()
			.map(dto -> Map.entry(dto.getCustomerName() + "," + dto.getPhoneNo(), dto))
			.filter(entry -> !existingKeys.contains(entry.getKey()))
			.distinct()
			.map(entry -> Customer.builder()
				.user(user)
				.name(entry.getValue().getCustomerName())
				.phoneNo(entry.getValue().getPhoneNo())
				.build())
			.toList();
	}

	private List<AgentProperty> mapToAgentProperties(List<AgentPropertyExcelDTO> list,
		Map<String, Customer> customerMap, User user) {
		log.info("[매물 매핑 시작] 대상 수 = {}", list.size());
		return list.stream().map(dto -> {
			GeoCodeResultVO geo = getGeoCode(dto.getRowNum(), dto.getRoadName());
			Customer customer = customerMap.get(dto.getCustomerName() + "," + dto.getPhoneNo());
			return AgentProperty.builder()
				.customer(customer)
				.user(user)
				.address(geo.getAddressName())
				.detailAddress(dto.getDetailAddress())
				.longitude(Double.valueOf(geo.getLongitude()))
				.latitude(Double.valueOf(geo.getLatitude()))
				.legalDistrictCode(geo.getLegalDistrictCode())
				.constructionYear(Year.parse(dto.getConstructionYear()))
				.floor(dto.getFloor())
				.deposit(dto.getDeposit())
				.monthlyRent(dto.getMonthlyRent())
				.price(dto.getPrice())
				.moveInDate(dto.getMoveInDate())
				.startDate(dto.getStartDate())
				.endDate(dto.getEndDate())
				.petsAllowed(dto.getPetsAllowed())
				.hasElevator(dto.getHasElevator())
				.parkingCapacity(dto.getParkingCapacity())
				.realCategory(PropertyCategory.valueOf(dto.getRealCategory()))
				.type(PropertyType.valueOf(dto.getType()))
				.totalArea(dto.getTotalArea())
				.netArea(dto.getNetArea())
				.details(dto.getDetails())
				.build();
		}).toList();
	}

	private List<AgentProperty> mapToAgentPropertiesAsync(List<AgentPropertyExcelDTO> list,
		Map<String, Customer> customerMap, User user) {
		log.info("[매물 매핑 시작] 대상 수 = {}", list.size());

		List<Mono<AgentProperty>> propertyMonos = list.stream()
			.map(dto -> {
				log.info("[1. 작업 정의] rowNum={}, address={}", dto.getRowNum(), dto.getRoadName());

				return getGeoCodeAsync(dto.getRowNum(), dto.getRoadName())
					.doOnSubscribe(
						sub -> log.info("[2. 요청 시작] rowNum={}, address={}", dto.getRowNum(), dto.getRoadName()))
					.map(geo -> {
						log.info("[3. 응답 수신] rowNum={}, 위도={}, 경도={}", dto.getRowNum(), geo.getLatitude(),
							geo.getLongitude());

						Customer customer = customerMap.get(dto.getCustomerName() + "," + dto.getPhoneNo());

						return AgentProperty.builder()
							.customer(customer)
							.user(user)
							.address(geo.getAddressName())
							.detailAddress(dto.getDetailAddress())
							.longitude(Double.valueOf(geo.getLongitude()))
							.latitude(Double.valueOf(geo.getLatitude()))
							.legalDistrictCode(geo.getLegalDistrictCode())
							.constructionYear(Year.parse(dto.getConstructionYear()))
							.floor(dto.getFloor())
							.deposit(dto.getDeposit())
							.monthlyRent(dto.getMonthlyRent())
							.price(dto.getPrice())
							.moveInDate(dto.getMoveInDate())
							.startDate(dto.getStartDate())
							.endDate(dto.getEndDate())
							.petsAllowed(dto.getPetsAllowed())
							.hasElevator(dto.getHasElevator())
							.parkingCapacity(dto.getParkingCapacity())
							.realCategory(PropertyCategory.valueOf(dto.getRealCategory()))
							.type(PropertyType.valueOf(dto.getType()))
							.totalArea(dto.getTotalArea())
							.netArea(dto.getNetArea())
							.details(dto.getDetails())
							.build();
					});
			})
			.toList();

		log.info("[병렬 호출 시작] 총 요청 수 = {}", propertyMonos.size());
		return Flux.fromIterable(propertyMonos)
			.delayElements(Duration.ofMillis(5)) // QPS 제한
			.flatMap(mono -> mono, 40)             // 병렬 제한
			.collectList()
			.doOnNext(result -> log.info("[병렬 호출 완료] 응답 수 = {}", result.size()))
			.block();
	}

	private List<Customer> mapToCustomerEntitiesAsync(List<CustomerExcelDTO> list, User user) {
		log.info("[고객 매핑 시작 - 비동기] 대상 수={}", list.size());

		List<Mono<Customer>> monoList = list.stream()
			.map(dto -> {
				Mono<String> legalCodeMono = (dto.getPreferredRegion() == null)
					? Mono.just((String)null)
					: getGeoCodeAsync(dto.getRowNum(), dto.getPreferredRegion())
					.map(GeoCodeResultVO::getLegalDistrictCode);

				return legalCodeMono.map(legalCode -> toCustomerEntity(dto, legalCode, user));
			})
			.toList();

		return Flux.fromIterable(monoList)
			.delayElements(Duration.ofMillis(5))
			.flatMap(Function.identity(), 30)
			.collectList()
			.block();
	}

	private GeoCodeResultVO getGeoCode(int rowNum, String roadName) {
		log.info("[Kakao API 요청] rowNum={}, 주소={}", rowNum, roadName);
		KakaoGeocodeResponseDTO response = kakaoGeoClient.getCoordinatesByAddress(roadName);
		if (response == null || response.getDocuments().isEmpty()) {
			log.warn("[Kakao API 실패] rowNum={}, 잘못된 주소 값", rowNum);
			throw new ExcelException(ExcelErrorCode.INVALID_INPUT_VALUE, rowNum, "roadName", roadName,
				"주소를 찾을 수 없습니다.");
		}

		KakaoGeocodeResponseDTO.Document first = response.getDocuments().get(0);
		if (!Strings.hasText(first.getAddress().getDongHName()) || !Strings.hasText(first.getAddress().getDongName())) {
			throw new ExcelException(ExcelErrorCode.INVALID_INPUT_VALUE, rowNum, "roadName", roadName,
				"해당하는 동 주소를 찾을 수 없습니다.");
		}

		String addressName = null;
		if (first.getAddress() != null) {
			if (first.getAddressType().equalsIgnoreCase("REGION_ADDR")) {
				addressName = first.getAddress().getJibunAddressName();
			}
			if (first.getAddressType().equalsIgnoreCase("ROAD_ADDR")) {
				addressName = first.getRoadAddress().getRoadAddressName();
			}
		}
		return new GeoCodeResultVO(
			first.getAddress() != null ? first.getAddress().getLegalDistrictCode() : null,
			first.getLongitude(),
			first.getLatitude(),
			addressName
		);
	}

	public Mono<GeoCodeResultVO> getGeoCodeAsync(int rowNum, String roadName) {
		log.info("[Kakao API 요청] rowNum={}, 주소={}", rowNum, roadName);

		return kakaoGeoAsyncClient.getCoordinatesByAddressAsync(roadName)
			.map(response -> {
				if (response == null || response.getDocuments().isEmpty()) {
					log.warn("[Kakao API 실패] rowNum={}, 잘못된 주소 값", rowNum);
					throw new ExcelException(ExcelErrorCode.INVALID_INPUT_VALUE, rowNum, "roadName", roadName,
						"주소를 찾을 수 없습니다.");
				}

				KakaoGeocodeResponseDTO.Document first = response.getDocuments().get(0);
				if (first.getAddress() == null ||
					!Strings.hasText(first.getAddress().getDongHName()) ||
					!Strings.hasText(first.getAddress().getDongName())) {

					throw new ExcelException(ExcelErrorCode.INVALID_INPUT_VALUE, rowNum, "roadName", roadName,
						"해당하는 동 주소를 찾을 수 없습니다.");
				}

				String addressName = null;
				if (first.getAddress() != null) {
					if (first.getAddressType().equalsIgnoreCase("REGION_ADDR")) {
						addressName = first.getAddress().getJibunAddressName();
					}
					if (first.getAddressType().equalsIgnoreCase("ROAD_ADDR")) {
						addressName = first.getRoadAddress().getRoadAddressName();
					}
				}

				return new GeoCodeResultVO(
					first.getAddress().getLegalDistrictCode(),
					first.getLongitude(),
					first.getLatitude(),
					addressName
				);
			});
	}

	private User getUserOrThrow(Long userUid) {
		return userRepository.findById(userUid)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	private List<Map<String, Object>> convertToDuplicatedCustomerDetails(List<Customer> duplicatedCustomers,
		Map<String, Integer> rowNumMap) {
		return duplicatedCustomers.stream().map(customer -> {
			String key = customer.getName() + "," + customer.getPhoneNo();
			Map<String, Object> detail = new HashMap<>();
			detail.put("rowNum", rowNumMap.getOrDefault(key, -1));
			detail.put("field", "name/phoneNo");
			detail.put("value", key);
			detail.put("message", "이미 존재하는 고객입니다.");
			return detail;
		}).toList();
	}
}
