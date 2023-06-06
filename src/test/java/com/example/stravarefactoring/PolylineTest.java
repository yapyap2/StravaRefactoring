package com.example.stravarefactoring;

import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PolylineTest {

    @Test
    public void polylineDecodingTest(){

        String polyline = "cgcfFeitjWGf@}ADSEUc@W}A?aB^e@He@gAwAoAo@o@s@OkA`@sCGa@kIuB}@c@Ys@@g@~@uFXcAp@eAjBmArA_BtBkEpAuDNcABqCo@gN}@kOSeKRo@y@kJaA_EyI{PsCcHyFePKk@kZt@eAJiCx@aBhAmBhBwg@bi@eEfFuBxBuCrB{BdAsJzDc@HYIo@aAuAaGmCsIaAeCqBsDwIqMuAaDwIwb@{CgQ_AcG_A{Cq@_BiCuD{DaDaEmCsA}As@uBaAoJcFkAwHcA{Mb@mDAc[aFoFb@gMzBsf@rDk@EUSKi@wB_XcAuGg@wB}CaH]{E{@qBUwAc@Ew@N{LlF_AJyImM}@_CcBqHoAeEyBwEaFeIsCiFqLuPqDgBqDoDmBaAYi@m@gCuBwAaAgAmAqBeAiAWmAc@c@JRFG}AgA_A{A{@q@q@JwA~AqATcBdAWb@[pB_@t@mCtBaAvA|@{Ac@`AYZe@Jg@WWs@PyCIUMhBHAFkAEkBdA}E?gC`BoC~AkAn@eAHi@Qs@uAo@i@sAWY}@QyCeBW[Og@PiFM[@`BUxBJn@h@h@r@Tw@]]k@NmDWlCNv@\\\\|Al@dC`Bf@tAvAx@V\\F`@g@nAaBfA{BrCO`@JxACfAy@pCEl@FlBQhCJn@l@^h@GhBwB|Ay@_Ar@w@ZeBlBg@GQSKwAlAyLAgCnB}CvA_Ah@_ALi@E[Ua@sAi@k@yAiBu@o@m@sAk@Ya@Gm@RqEbAcBmArBYfDFn@r@v@~Af@`CtAd@xAvAp@X|@Kh@Yb@i@l@aAj@qBhDArCWvAi@pBHrCOtBPr@h@V`@CrAuAhB_A~@kAd@aDZg@fBaAbA[bBiBx@Gd@T~@dBdB~An@rBvBfC|@`BhCjBNXRlAb@x@lBnAxD~D`B`ApBxBrFxHrEnHn@tAdGbKjDzIpAzFj@fBtCnFrDdF^bAPjAd@dAjArPTfQWnU}Bt]J~]|AhR|@~T^nDr@fDdWzz@?n@RZfCIva@jE~CDbEc@d@c@nHwCrOmBjj@sAzK{@vP?vJ{@vEqA~ZgLbCk@^PP\\j@~Cv@nC|BjEpAnBtWxJjF`CpUfIX`@ZC`CmA~HsFh@QdFiD\\AtFvOzBxHr@tAFZd@_@xBa@r@]xIlFvC|BjCF`IzBJ\\a@fC@`@`@n@jBbA`AbBm@pBGnAB|Bb@`@fCMTYIqA";
        String newEncodedString = polyline.replace("\\\\", "\\");
        List<LatLng> latLngList = PolylineEncoding.decode(newEncodedString);

        latLngList.forEach(l -> System.out.println(l));
    }
}
