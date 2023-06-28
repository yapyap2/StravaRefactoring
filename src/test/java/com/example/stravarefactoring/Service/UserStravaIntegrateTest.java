package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.Repository.UserJDBCRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.config.testConfig;
import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = testConfig.class)
public class UserStravaIntegrateTest {
    @Autowired
    ApplicationContext applicationContext;
    Token token;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    @Qualifier("parallelLocationMapper")
    private ParallelLocationMapper locationMapper;
    @Autowired
    LocationQueue locationQueue;
    @Autowired
    private StravaApiClient client;

    @Autowired
    UserJDBCRepository userJDBCRepository;

    WebClient webClient = WebClient.builder().build();

    StravaModifier stravaModifier;

    @Autowired
    EntityManager entityManager;

    @BeforeAll
    public void getToken() throws SQLException, ClassNotFoundException {
        stravaModifier = new StravaModifier();
        token = stravaModifier.getToken(1);
    }


    @Test
    @Transactional
    public void newUserTest(){
        User user = userService.addUser(token);

        User findUser  = userRepository.findUserById(token.getId());

        assertTrue(findUser != null);
        assertTrue(user.getRides().size() == rideRepository.findAllByUserId(user.getId()).size());

    }

    @Transactional
    @Test
    @DirtiesContext
    public void duplicatedUserProfileUpdate(){
        userService = applicationContext.getBean("userServiceMockMapper", UserService.class);

        User user1 = userService.addUser(token);
        entityManager.detach(user1);

        Random r = new Random();
        double w = Math.round(r.nextDouble(80 - 60 - 1) + 60);
        stravaModifier.updateProfile(token, w);

        User user2 = userService.addUser(token);

        assertTrue(user2.getWeight() == w);
        assertTrue(user2.getWeight() != user1.getWeight());
        assertTrue(user1.getRides().size() == user2.getRides().size());
    }

    @Test
    @Transactional
    @DirtiesContext
    public void duplicatedUserNoUpdateTest(){
        userService = applicationContext.getBean("userServiceMockMapper", UserService.class);

        User user1 = userService.addUser(token);
        
        awaitTermination();
        
        User user2 = userService.addUser(token);
        
        assertTrue(user1.getRides().size() == user2.getRides().size());

    }


    @Test
    @Transactional
    @DirtiesContext
    public void duplicatedUserRideUpdate() {
        userService = applicationContext.getBean("userServiceMockMapper", UserService.class);

        User user1 = userService.addUser(token);
        int user1Ride = user1.getRides().size();
        LocalDateTime user1Time = user1.getLastUpdated();

        stravaModifier.addRide(token);

        User user2 = userService.addUser(token);
        int user2Ride = user2.getRides().size();
        LocalDateTime user2Time = user2.getLastUpdated();

        assertTrue(user2Ride == user1Ride + 1);
        assertTrue(user2Time.isAfter(user1Time));

        List<Ride> findRide = rideRepository.findAllByUserId(user2.getId());
        assertTrue(findRide.size() == user1Ride + 1);

    }

    @Test  //여기에 Transactional을 걸면 안되는 이유는 userService.addUser() 종료 이후 locationMapper에서 user 엔티티를 사용해야 하는데
            // 테스트 메소드 단의 @Transactional로 인해 user 엔티티가 DB에 실제 반영되지 않고 Lock이 걸려있음 이때 LocationMapper는 별도의 트랜잭션을 사용하기 때문에(서브 스레드니까)
            // lock이 풀릴때까지 대기해야 함   정확한 설명은 Notion 으로
    public void locationTest() throws SQLException, ClassNotFoundException, InterruptedException {
        token = stravaModifier.getToken(1);

        User user1 = userService.addUser(token);
//        entityManager.detach(user1);

        assertTrue(user1 != null);
        assertTrue(user1.getRides().size() > 0);
        awaitTermination();

        User findUser2 = userRepository.findUserById(user1.getId());

        assertTrue(findUser2.getLocation().size() != 0);

        rideRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Autowired
    RideRepository repository;
    @Autowired
    DataSource dataSource;
    @Test //위의 이유와 동일하게 @Transactional을 사용해선 안됨
    public void newLocationTest() throws InterruptedException {
        userService.addUser(token);

        awaitTermination();

        User user1 = userRepository.findUserById(token.getId());

        StravaApiClient mockClient = mock(StravaApiClient.class);

        StravaService stravaService = new StravaService(mockClient, new RideBatchRepository(new JdbcTemplate(dataSource)), repository);


        userService = new UserService(userRepository, mockClient, stravaService, locationMapper, locationQueue);
        Ride ride = new Ride();
        User u = new User();
        u.setName("yap test");
        ride.setUser(u);
        ride.setName("new Location Ride");
        ride.setSummary_polyline("wsefFs|ujW{~@qXuc@ugA_SybAoTcVkCic@pM_oAuKw{@vFwKuSa[hEiUjMwVrKO|EfZjOud@`L`Mbx@`K|UyTrFlOjLmB~I_PlDuy@rZuW}BkThSmQdUrM_Ac[lI{OlfAu}@lLq[{CoLlAa]tKzGfLuO`EvLdIwG~g@gtBpu@sFdk@qVd`@mo@lr@y]lEiYfTzM~H__@p_Ath@vU}a@j^cBtJdOlKiOz^_AwCoh@zx@qH~Dgk@mL}Xvb@{y@bn@pIz_@wVrFiRvIe{@_u@yZcGwvBcRa_Ar`@{gAsP{eAe@{|@nJy`A`D_OpQ}H{Cu\\hWuQsVcn@pn@{jAuIud@h]svClc@cnA|MaLne@goBdd@c`@fM{l@}Fa_@cb@mh@_C_nApr@clApk@yh@bGuT_MyEyD`Ll[wn@jJmt@cHkj@nNs~@xp@i`AfGkb@}m@qj@hG}g@sAgXqg@cr@sMklAcTuHwqA}}Aav@iSiu@ggAgHeIaOMxEy`@fh@}rAoJuf@}K_IdL_KrA}w@`SsMfItIxRkzAmw@ux@mp@yRoKa~@iS{QiJrGhJ_Hq_@wh@lu@uQblAar@fe@zLt\\cUliAcvAuDog@pL_p@qJuJpGtA}F_QxLsBuRyL|x@uXpQdAcc@xQih@bDfMjD~DjGaMhBjGpPkHiArKhJaMvp@~Dvf@wm@n{@_w@lo@og@kLakA|q@cs@~LH`Nn\\n^wPpHua@qFk_@{]ed@gImKgRkaAwL{i@cq@_a@tAqq@aWaVi`AiaAze@}h@}b@gTkAsEgPr\\aOrRk^vGel@aUwm@lYse@nAa\\oZsUzf@ii@xAqPiHcGyVnT}V_DkIuTjJ_\\ct@wLg`@aZgKglA{Xur@yGgfA}VnH{KnV{G}\\sNq@yEmTy[~XoV}O}F~Lu`@D{l@cYkCg[oY|LaGm]gWvDaJqMxH_PoCkKqRlD?eTaV}FvGjJuMPsM~g@hR_K}Rfs@~LqDiXzg@oFd`@iHiE|FtKfIkHlBpNpKeCmm@lg@}ZlFqNmLes@nl@{g@vGapBcgA{]gl@g_@kXgnAg]oi@_p@kh@yIuyAs{@}_Dav@mUss@oQbW_ToIer@kgAaXrKqY{D}JoJ{UwrAoYc_@am@yWoRwr@zMw~@{k@irAefAaJgPkd@iiAkZs@uh@k\\|\\eFqGfDnI|GuG");
        ride.setStart_date_local(LocalDateTime.now());

        when(mockClient.getOneRide(anyString())).thenReturn(List.of(ride));
        when(mockClient.getUserInfo(any(Token.class))).thenReturn(client.getUserInfo(token));

        Answer<List<Ride>> answer = new Answer<List<Ride>>() {
            int count = 1;

            @Override
            public List<Ride> answer(InvocationOnMock invocation) throws Throwable {
                if (count == 1) {
                    count++;
                    return List.of(ride);
                } else return new ArrayList<>();
            }
        };
        when(mockClient.getRideAfter(anyString(), anyInt(), any(LocalDateTime.class))).thenAnswer(answer);


        userService.addUser(token);

        Thread.sleep(2000);

        awaitTermination();

        User user2 = userRepository.findUserById(token.getId());

        assertTrue(user1.getLocation().size() < user2.getLocation().size());

        rideRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

//    @Test
//    public void rollBackTest(){
//        RideBatchRepository rideBatchRepository = mock(RideBatchRepository.class);
//        ParallelLocationMapper mockMapper = mock(ParallelLocationMapper.class);
//
//        StravaService stravaService = new StravaService(client, rideBatchRepository, repository);
//
//
//        userService = new UserService(userRepository, client, stravaService, mockMapper, rideRepository,userJDBCRepository);
//
//        doThrow(new RuntimeException()).when(rideBatchRepository).saveAll(anyList());
//
//        assertThrows(RuntimeException.class, () -> {
//            try{
//                userService.addUser(token);
//            } catch (RuntimeException e){
//                e.printStackTrace();
//                throw e;
//            }
//        });
//
//        assertTrue(userRepository.findUserById(token.getId()) == null);
//        assertTrue(rideRepository.findAllByUserId(token.getId()) == null);
//    }


    private void awaitTermination() {
        ThreadPoolTaskExecutor taskExecutor = applicationContext.getBean("MapperAsyncExecutor", ThreadPoolTaskExecutor.class);

        while (true) {
            if (taskExecutor.getActiveCount() == 0) {
                break;
            }
        }
    }
}
