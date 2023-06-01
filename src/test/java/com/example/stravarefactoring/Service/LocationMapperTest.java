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

    String polyline = "e|adF}lnfWlj@wjEgdBq{By~CcjBqk@saDk}@ovCae@euA`VcyAp_BkyBx~CgiBp~Co{HeTur@auBeYsq@i}@xbC}pIrfCamBkp@oeCnOmxDltBulHh_BatAlcFex@rq@eeDpW`GvpGuxBnq@tK~yA_sAxhCinGjsAew@jVx}@rcDieBjp@s~HjnBu|@byBdj@pBk~AprAwbAWieA_g@cQxf@uaFlaC|BhwBt_AfnGoi@hvC|dAtcEobCzfBhy@~|F_sA|LojAjsEtYdyAyu@hl@sbBtjKvvDbn@ot@li@bKxh@a~CxdEogJnfD|uEncAqN|_CdnCqDb_@j_B~eAsIdkDph@~v@ax@zdBtVniBqe@hnD{a@hdA}ShyFl^pcBzaB|j@x[|vAuPnw@|}Cb{L{CxdB~lEzzCn|A~Ap_EhvD|t@xcHt{HbzHpzClu@nuAbtAhj@fcDzhEbd@gi@fhA}b@Bdj@nf@c[rp@lR`pFvy@dhB}Efj@zl@hLzyFx{HlL~qApvE`hBb[z`Awl@pn@``D`}BpDloBfdEx~Br~Eb}HbeDvUlsAvz@byBxiHrbC~tAzv@aLbBlmApl@vgCv|DhWpwAd}AfIt_BlsAxvA{@jnCtbAlvEx}@xnBrp@ow@~gAh}Ap`DiVnpCjy@nvDfeGloCfiA{Y`kAe_AvXqqGwbBcuE`i@}rCdeC}T~iD}|Az}Cm~E`}DikBBskG_vBmmAzzAovAqj@_oBv~AufEyFkz@bzBajEqfAmJj~@ojBl{AbPpqBem@dr@iuGsHclAzt@u_@frEsgBk[qsChaCen@ab@cxEpnBecBumCspCbzAojGqFulDleA_o@fyAyVukAwnA|FaGgbB{oBolC}t@sk@_y@`Qtl@snDmQmbGssFkbBnWucAfeBg}AbdAkgBhK{aAvuCeaBm_AcwDd{A_wM`v@}bBmtAepAwLm}BgsAdOka@g}AmlAd@yr@_mAonBe`Bzf@cvCiuBi[i{ApeAihA{g@yxAh~@ahCyuF_YysCkn@wH_A_|DyeCsnB_L_aBqhCyw@wfGn{Aa_Ay\\\\gFiq@hnAobE{DopA_wA|EieBjx@eBpdAa{AzXaz@lkB}dBmN}zDtWyqAj`B}V_pH_`AwrCikEsnA}tAwHymAqbBedCuW|@g~@{bDQ_~BitAueBjoAeeCi~@s}Bfg@{pHm`Bys@et@sdAnoAaoB\\\\wiCm`B{qDll@alGuc@usBfuAyeBtyFutAkDm[mhEu{@rf@ph@|bBuq@tvC";
    @Test
    public void decodingTest(){
        List<LatLng> list = mapper.decode(polyline);

        list.forEach(i -> System.out.println(i));

    }

    @Test
    public void addressDecodingTest(){
        mapper.getAddress(polyline);
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
