package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.domain.Ride;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootTest
public class LocationMapperTest {

    @Autowired
    LocationMapper mapper;

    @BeforeEach
    public void before() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        rideList = objectMapper.readValue(new File("src/main/resources/static/activities.json"), new TypeReference<List<Ride>>() {});

        rideList = rideList.subList(0, 5);
    }
    List<Ride> rideList;

    String polyline = "wsefFs|ujW{~@qXuc@ugA_SybAoTcVkCic@pM_oAuKw{@vFwKuSa[hEiUjMwVrKO|EfZjOud@`L`Mbx@`K|UyTrFlOjLmB~I_PlDuy@rZuW}BkThSmQdUrM_Ac[lI{OlfAu}@lLq[{CoLlAa]tKzGfLuO`EvLdIwG~g@gtBpu@sFdk@qVd`@mo@lr@y]lEiYfTzM~H__@p_Ath@vU}a@j^cBtJdOlKiOz^_AwCoh@zx@qH~Dgk@mL}Xvb@{y@bn@pIz_@wVrFiRvIe{@_u@yZcGwvBcRa_Ar`@{gAsP{eAe@{|@nJy`A`D_OpQ}H{Cu\\hWuQsVcn@pn@{jAuIud@h]svClc@cnA|MaLne@goBdd@c`@fM{l@}Fa_@cb@mh@_C_nApr@clApk@yh@bGuT_MyEyD`Ll[wn@jJmt@cHkj@nNs~@xp@i`AfGkb@}m@qj@hG}g@sAgXqg@cr@sMklAcTuHwqA}}Aav@iSiu@ggAgHeIaOMxEy`@fh@}rAoJuf@}K_IdL_KrA}w@`SsMfItIxRkzAmw@ux@mp@yRoKa~@iS{QiJrGhJ_Hq_@wh@lu@uQblAar@fe@zLt\\cUliAcvAuDog@pL_p@qJuJpGtA}F_QxLsBuRyL|x@uXpQdAcc@xQih@bDfMjD~DjGaMhBjGpPkHiArKhJaMvp@~Dvf@wm@n{@_w@lo@og@kLakA|q@cs@~LH`Nn\\n^wPpHua@qFk_@{]ed@gImKgRkaAwL{i@cq@_a@tAqq@aWaVi`AiaAze@}h@}b@gTkAsEgPr\\aOrRk^vGel@aUwm@lYse@nAa\\oZsUzf@ii@xAqPiHcGyVnT}V_DkIuTjJ_\\ct@wLg`@aZgKglA{Xur@yGgfA}VnH{KnV{G}\\sNq@yEmTy[~XoV}O}F~Lu`@D{l@cYkCg[oY|LaGm]gWvDaJqMxH_PoCkKqRlD?eTaV}FvGjJuMPsM~g@hR_K}Rfs@~LqDiXzg@oFd`@iHiE|FtKfIkHlBpNpKeCmm@lg@}ZlFqNmLes@nl@{g@vGapBcgA{]gl@g_@kXgnAg]oi@_p@kh@yIuyAs{@}_Dav@mUss@oQbW_ToIer@kgAaXrKqY{D}JoJ{UwrAoYc_@am@yWoRwr@zMw~@{k@irAefAaJgPkd@iiAkZs@uh@k\\|\\eFqGfDnI|GuG";

    @Test
    public void decodingTest(){
        List<LatLng> list = mapper.decode(polyline);

        list.forEach(i -> System.out.println(i));

    }

    @Test
    public void addressDecodingTest(){
        mapper.getAddress(polyline).forEach(a -> System.out.println(a));
    }


    @Test
    public void asyncTest() throws InterruptedException {

        CompletableFuture<HashSet<String>> future = mapper.getLocation(rideList);

        log.info("before forEach");

        future.thenAccept(set -> set.forEach(s -> log.info("{} {}", Thread.currentThread().getName(), s)));

        log.info("{} 20 sec timer start", Thread.currentThread().getName());

        Thread.sleep(20000);

        log.info("{} 20 sec timer end", Thread.currentThread().getName());

    }

}
