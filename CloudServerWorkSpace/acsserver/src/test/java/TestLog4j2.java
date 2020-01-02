import com.github.amsacode.predict4java.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/*
 * 参考站点：https://blog.csdn.net/chenh_96/article/details/79819764
 * 两行根数，通过以下链接获取TLE数据：
 * http://www.celestrak.com/NORAD/elements/resource.txt
 * */
public class TestLog4j2 {
    private Logger logger = LogManager.getLogger(TestLog4j2.class);

    @Test
    public void testLog4j2() {
        logger.trace("trace message");
        logger.debug("debug message");
        logger.info("info message");
        logger.warn("warn message");
        logger.error("error message");
        logger.fatal("fatal message");
        System.out.println("Hello World!");
        System.out.println("当前时间:" + System.currentTimeMillis());
    }

    @Test
    public void calc() {
        StringBuffer sb = new StringBuffer();
        sb.append("SCD 1,1 22490U 93009B   19198.70208112  .00000211  00000-0  79241-5 0  9999,2 22490  24.9700 300.1604 0043212  83.5512  64.7428 14.44554031395684,TECHSAT 1B (GO-32),1 25397U 98043D   19198.36439073 -.00000033  00000-0  49648-5 0  9996,2 25397  98.6896 138.1628 0000993  14.2198 345.9008 14.23667720 91736,SCD 2,1 25504U 98060A   19198.88760191  .00000196  00000-0  43741-5 0  9997,2 25504  24.9946 146.9490 0017500 108.6378 282.7204 14.44071772 94769,LANDSAT 7,1 25682U 99020A   19199.18029852 -.00000003  00000-0  91181-5 0  9996,2 25682  98.1268 265.8638 0001307  99.3160 260.8190 14.57153709 77413,DLR-TUBSAT,1 25757U 99029B   19198.63404356 -.00000017  00000-0  71320-5 0  9991,2 25757  98.4116 120.7015 0015191 131.4035 228.8468 14.51403893 66780,TERRA,1 25994U 99068A   19199.20049652 -.00000052  00000-0 -14479-5 0  9997,2 25994  98.2039 273.3000 0001535  90.2932 269.8382 14.57112117 41492,MAROC-TUBSAT,1 27004U 01056D   19198.61921595 -.00000041  00000-0  30811-5 0  9995,2 27004  99.4689 331.7617 0020142 162.2973 265.6814 13.70135603880183,AQUA,1 27424U 02022A   19199.21542144  .00000016  00000-0  13599-4 0  9995,2 27424  98.2041 139.6175 0001503  12.0178 324.8030 14.57111543915091,IRS-P6 (RESOURCESAT-1),1 28051U 03046A   19199.19502900 -.00000019  00000-0  89180-5 0  9996,2 28051  98.5172 276.4058 0057229 301.5499  58.0113 14.34169277818333,SHIYAN 1 (SY-1),1 28220U 04012A   19199.00111929 -.00000587  00000-0 -42460-4 0  9995,2 28220  97.8028 161.3167 0008970  67.4085 292.8086 14.99853277830448,FORMOSAT-2 (ROCSAT 2),1 28254U 04018A   19199.14710494  .00000107  00000-0  10000-3 0  9995,2 28254  98.7688 242.2669 0002679 156.5599 268.7230 14.00725173775124,AURA,1 28376U 04026A   19198.88206964  .00000043  00000-0  19580-4 0  9993,2 28376  98.2037 141.4466 0001333  70.3860 289.7483 14.57116760798055,IRS-P5 (CARTOSAT-1),1 28649U 05017A   19199.18612507 -.00000000  00000-0  63350-5 0  9994,2 28649  97.8538 269.4035 0002677  91.4998 268.6529 14.83644906768804,SINAH 1,1 28893U 05043D   19198.45889798 -.00000022  00000-0  47360-5 0  9998,2 28893  97.9776 334.8052 0013852 279.7298  80.2339 14.61506869731502,EROS B,1 29079U 06014A   19199.19855535  .00000878  00000-0  39263-4 0  9998,2 29079  97.4238 317.7318 0004156  49.9039  67.8323 15.23943804734194,RESURS-DK 1,1 29228U 06021A   19198.87826533 -.00000004  00000-0  70677-5 0  9999,2 29228  69.9366 322.2233 0007678 261.7335  98.2955 15.02450172722349,ARIRANG-2 (KOMPSAT-2),1 29268U 06031A   19199.18619118 -.00000007  00000-0  76798-5 0  9991,2 29268  98.0291  90.5683 0018760 131.1541 229.1287 14.62438448692212,LAPAN-TUBSAT,1 29709U 07001A   19199.18848546 -.00000017  00000-0  43866-5 0  9999,2 29709  97.8096 160.7443 0013863 122.9231 237.3321 14.82180259676701,CARTOSAT-2 (IRS-P7),1 29710U 07001B   19199.19161913 -.00000028  00000-0  32569-5 0  9998,2 29710  97.9721 257.9716 0000984  92.0780 268.0571 14.78643969675655,HAIYANG-1B,1 31113U 07010A   19198.88907650 -.00000004  00000-0  14995-4 0  9995,2 31113  98.4518 246.8686 0014952 121.3082 238.9567 14.30225921640182,COSMO-SKYMED 1,1 31598U 07023A   19199.15292637  .00000304  00000-0  44806-4 0  9995,2 31598  97.8898  22.1114 0001285  77.1504 282.9832 14.82155687655162,TERRASAR-X,1 31698U 07026A   19198.83655300  .00000411  00000-0  22750-4 0  9996,2 31698  97.4452 205.6462 0001585  98.4053  24.7280 15.19154021670345,WORLDVIEW-1 (WV-1),1 32060U 07041A   19198.95116953  .00000344  00000-0  16902-4 0  9993,2 32060  97.3940 318.1997 0002368  92.3322 267.8187 15.24413410658152,YAOGAN 3,1 32289U 07055A   19199.18765341 -.00000161  00000-0 -13988-4 0  9998,2 32289  98.0021 209.8083 0001357  72.8300 287.3065 14.80964323631362,COSMO-SKYMED 2,1 32376U 07059A   19199.18665884 -.00000373  00000-0 -40265-4 0  9993,2 32376  97.8900  22.1449 0001438  86.7514 273.3845 14.82154173627918,RADARSAT-2,1 32382U 07061A   19199.24210854  .00000061  00000-0  40704-4 0  9993,2 32382  98.5754 205.7210 0001065  96.2138 320.8012 14.29984944605063,CARTOSAT-2A,1 32783U 08021A   19199.17001922 -.00000113  00000-0 -82204-5 0  9993,2 32783  97.9705 258.3114 0013303  18.5318 341.6375 14.78652563605593,JASON-2 (OSTM),1 33105U 08032A   19198.94966760 -.00000054  00000-0  31457-4 0  9994,2 33105  66.0425 322.6299 0008168 267.2132 202.3537 12.87627733518479,RAPIDEYE 2,1 33312U 08040A   19198.63088103  .00000366  00000-0  50550-4 0  9995,2 33312  97.7433 266.5292 0011713  28.0277 332.1163 14.83647012587867,RAPIDEYE 5,1 33313U 08040B   19198.65472247  .00000808  00000-0  10405-3 0  9999,2 33313  97.7489 265.4340 0018378 272.1732  87.8165 14.83650193587818,RAPIDEYE 1,1 33314U 08040C   19198.64300687  .00000248  00000-0  36285-4 0  9990,2 33314  97.7471 266.3244 0024296  48.2775 312.0339 14.83645960587854,RAPIDEYE 3,1 33315U 08040D   19199.08324225  .00000826  00000-0  10624-3 0  9994,2 33315  97.7509 265.7410 0012309 342.6416  17.4395 14.83646011587889,RAPIDEYE 4,1 33316U 08040E   19198.66624133  .00000243  00000-0  35771-4 0  9993,2 33316  97.7540 265.2135 0012082 290.5557  69.4120 14.83645050587817,HUANJING 1A (HJ-1A),1 33320U 08041A   19199.14029677  .00000058  00000-0  15346-4 0  9995,2 33320  97.8368 256.7044 0027045  84.6566 275.7745 14.77030002585105,HUANJING 1B (HJ-1B),1 33321U 08041B   19199.16654463  .00000078  00000-0  18141-4 0  9993,2 33321  97.8442 258.6366 0035963 103.0955 257.4278 14.77144216585104,GEOEYE 1,1 33331U 08042A   19199.16538975  .00000037  00000-0  15985-4 0  9992,2 33331  98.0969 273.1933 0009932  39.2959 320.8981 14.64394976580461,THEOS,1 33396U 08049A   19199.20647398  .00000169  00000-0  10000-3 0  9993,2 33396  98.6841 265.6743 0001059 101.7747 324.0532 14.20050353559480,COSMO-SKYMED 3,1 33412U 08054A   19199.16980346  .00000569  00000-0  77991-4 0  9993,2 33412  97.8896  22.1335 0001565  84.2574 275.8846 14.82155850580377,YAOGAN 4,1 33446U 08061A   19198.99960626  .00000035  00000-0  11875-4 0  9994,2 33446  97.6764 164.7298 0016003 354.6369   5.4668 14.76962492572311,GOSAT (IBUKI),1 33492U 09002A   19199.21054862  .00000040  00000-0  15504-4 0  9992,2 33492  98.0376 310.9928 0001671  88.4312 271.7084 14.67566389561443,YAOGAN 6,1 34839U 09021A   19199.15516038  .00000287  00000-0  13434-4 0  9993,2 34839  97.0527 221.4076 0029424 133.1943 227.1762 15.26597682568654,DEIMOS-1,1 35681U 09041A   19199.19279031  .00000076  00000-0  19711-4 0  9994,2 35681  97.8595  80.0654 0002581  66.1679 293.9799 14.71792652535074,DUBAISAT-1,1 35682U 09041B   19199.09727091  .00000109  00000-0  26497-4 0  9994,2 35682  97.7294  30.9038 0010492 236.9662 123.0506 14.68739027533899,OCEANSAT-2,1 35931U 09051A   19198.85911414  .00000026  00000-0  17685-4 0  9996,2 35931  98.3017 293.5809 0002091  41.2985 318.8367 14.50868819519650,WORLDVIEW-2 (WV-2),1 35946U 09055A   19199.22858170 -.00000063  00000-0 -64422-5 0  9992,2 35946  98.4351 274.3501 0000504 297.9081  62.2068 14.37671904512853,SMOS,1 36036U 09059A   19199.13995165  .00000009  00000-0  16861-4 0  9994,2 36036  98.4445  25.9692 0001295  91.9453 268.1883 14.39756961510110,YAOGAN 7,1 36110U 09069A   19199.12855516  .00000032  00000-0  12838-4 0  9998,2 36110  98.2716  22.5875 0023459 204.5472 155.4615 14.75123445517064,TANDEM-X,1 36605U 10030A   19198.83644970 -.00006119  00000-0 -28880-3 0  9991,2 36605  97.4479 205.6443 0001452  99.9764  22.2971 15.19105886503085,CARTOSAT-2B,1 36795U 10035A   19199.13899856  .00000025  00000-0  10520-4 0  9996,2 36795  97.9683 257.9113 0015783  87.4911 272.8118 14.78648165486632,YAOGAN 10,1 36834U 10038A   19199.15977993  .00000010  00000-0  80516-5 0  9991,2 36834  97.9669 199.2671 0001403  75.0820 285.0546 14.80923632483101,COSMO-SKYMED 4,1 37216U 10060A   19199.13178419  .00000010  00000-0  78789-5 0  9994,2 37216  97.8903  22.0904 0001296  76.3076 283.8271 14.82156274470452,RESOURCESAT-2,1 37387U 11015A   19199.19940458 -.00000015  00000-0  13039-4 0  9992,2 37387  98.7145 273.9243 0003112 273.7469  86.3353 14.21630559427804,HAIYANG 2A,1 37781U 11043A   19198.82920685 -.00000043  00000-0  57006-6 0  9995,2 37781  99.3170 207.2829 0001160 118.4112 241.7140 13.78720256398799,RASAT,1 37791U 11044D   19199.09759008  .00000003  00000-0  96510-5 0  9993,2 37791  98.1193 291.5712 0020792 245.1675 114.7371 14.64725026423153,MEGHA-TROPIQUES,1 37838U 11058A   19199.19478557  .00000294  00000-0  17214-4 0  9993,2 37838  19.9764 234.7615 0009638 124.8805 235.2506 14.09689269400617,SRMSAT,1 37841U 11058D   19199.21369566  .00000269  00000-0  26681-5 0  9990,2 37841  19.9706 213.1398 0011845 159.9609 275.9676 14.10635080400860,YAOGAN 13,1 37941U 11072A   19199.17436838  .00000153  00000-0  10831-4 0  9990,2 37941  97.5279 130.8478 0002854 115.8428 355.9253 15.18023060423102,PLEIADES 1A,1 38012U 11076F   19199.20351250 -.00000042  00000-0  70451-6 0  9996,2 38012  98.1979 273.5110 0001444  80.9108 279.2263 14.58549845403794,ZIYUAN 1-02C (ZY 1-02C),1 38038U 11079A   19199.09022687  .00396346  00000-0  13256+0 0  9990,2 38038  98.5606 243.5905 0005719 163.4685 196.6803 14.35296847396663,ZIYUAN 3 (ZY 3),1 38046U 12001A   19199.15261997  .00000241  00000-0  13885-4 0  9990,2 38046  97.4542 272.5375 0001971 298.9365 144.5947 15.21293359417641,RISAT-1,1 38248U 12017A   19199.12878573  .00000095  00000-0  98009-5 0  9991,2 38248  97.5693 207.4351 0003328 336.3954  23.7122 15.09841661398038,ARIRANG-3 (KOMPSAT-3),1 38338U 12025B   19199.08750605  .00000016  00000-0  12598-4 0  9991,2 38338  98.1618 136.5731 0010875 107.1449 253.0947 14.61761166382331,KANOPUS-V 1,1 38707U 12039A   19199.15517638  .00000172  00000-0  11404-4 0  9999,2 38707  97.4555 117.3797 0001364  84.8371  48.0152 15.19028054387601,EXACTVIEW-1 (ADS-1B),1 38709U 12039C   19194.88620549 -.00000048  00000-0  14409-6 0  9995,2 38709  99.1367 210.2599 0009910 243.6140 116.4029 14.23995197362522,SPOT 6,1 38755U 12047A   19199.15096149 -.00000044  00000-0  43032-6 0  9993,2 38755  98.2094 265.8848 0001314  95.1488 264.9878 14.58546745364867,PLEIADES 1B,1 39019U 12068A   19199.10060658 -.00000054  00000-0 -17699-5 0  9992,2 39019  98.1968 273.3907 0001281  84.6637 275.4710 14.58556088352637,GOKTURK 2,1 39030U 12073A   19199.15265964  .00000008  00000-0  90413-5 0  9995,2 39030  97.9907  88.1977 0002168 118.5938 241.5494 14.72740827352551,LANDSAT 8,1 39084U 13008A   19199.15401328  .00000008  00000-0  11934-4 0  9999,2 39084  98.2230 268.7536 0001201  92.2598 267.8749 14.57109596341879,SARAL,1 39086U 13009A   19199.14317182 -.00000024  00000-0  73403-5 0  9994,2 39086  98.5417  24.8712 0001727 120.9620 239.1734 14.32007784334019,GAOFEN 1,1 39150U 13018A   19199.21537507  .00000028  00000-0  11217-4 0  9996,2 39150  97.8990 278.9326 0020082 118.9319 241.3915 14.76533357335542,VNREDSAT 1,1 39160U 13021B   19199.16872554  .00000004  00000-0  99674-5 0  9997,2 39160  98.1818 274.2949 0000904 117.9408 242.1900 14.62872760330867,ARIRANG-5 (KOMPSAT-5),1 39227U 13042A   19199.13014782  .00000269  00000-0  23555-4 0  9999,2 39227  97.6197  22.8940 0001897  88.5630  33.0653 15.04516884324106,YAOGAN 18,1 39363U 13059A   19199.14177274  .00000460  00000-0  25656-4 0  9992,2 39363  97.2964 255.3670 0002695  70.4009  13.8352 15.18163942316768,SKYSAT-1,1 39418U 13066C   19199.15700164  .00000021  00000-0  66366-5 0  9999,2 39418  97.6496 280.4557 0020862 245.6702 114.2343 14.98833612309053,DUBAISAT-2,1 39419U 13066D   19199.18584920 -.00000051  00000-0  44246-6 0  9991,2 39419  97.5896 260.5173 0012542 246.5144 113.4767 14.94003493308115,GPM-CORE,1 39574U 14009C   19198.90047872  .00000813  00000-0  16225-4 0  9999,2 39574  65.0041 212.8774 0011261 286.3829  73.6079 15.55487877305845,SENTINEL-1A,1 39634U 14016A   19199.05889004 -.00000090  00000-0 -93182-5 0  9999,2 39634  98.1825 205.8330 0001194  79.0708 281.0630 14.59196798281658,KAZEOSAT 1,1 39731U 14024A   19199.18070184 -.00000026  00000-0  52747-5 0  9991,2 39731  98.3532 275.0492 0000929  81.4210 278.7091 14.42044488274567,ALOS-2,1 39766U 14029A   19199.18744208 -.00000221  00000-0 -22734-4 0  9999,2 39766  97.9225 295.5896 0001293  80.1204 280.0170 14.79468829278127,KAZEOSAT 2,1 40010U 14033A   19199.14281250  .00000020  00000-0  91297-5 0  9993,2 40010  97.8370  97.7055 0015512 321.6650  38.3469 14.82065018274653,HODOYOSHI-4,1 40011U 14033B   19199.11584566  .00000053  00000-0  13562-4 0  9991,2 40011  97.8141  91.5260 0026125 317.4106  42.5092 14.81113706274387,DEIMOS-2,1 40013U 14033D   19198.51231103 -.00000053  00000-0 -64214-7 0  9997,2 40013  97.8390 102.1138 0002010  51.4604 308.6819 14.84544277275022,HODOYOSHI-3,1 40015U 14033F   19198.72755313  .00000040  00000-0  12535-4 0  9996,2 40015  97.7839  81.1546 0036378 333.1495  26.7833 14.78504702273824,SPOT 7,1 40053U 14034A   19199.18386844 -.00000041  00000-0  10413-5 0  9995,2 40053  98.2258 265.5304 0000913  76.7808 283.3500 14.58536570268801,SKYSAT-2,1 40072U 14037D   19198.97301397  .00000035  00000-0  11975-4 0  9990,2 40072  98.4306 354.2870 0007517 124.1094 236.0831 14.81193226271599,WORLDVIEW-3 (WV-3),1 40115U 14048A   19199.15245977  .00000076  00000-0  15050-4 0  9995,2 40115  97.8106 277.2715 0001270  44.2104 315.9213 14.85020729267030,GAOFEN 2,1 40118U 14049A   19199.19520342  .00000027  00000-0  10313-4 0  9998,2 40118  97.8705 278.4377 0007762 170.1232 190.0150 14.80633630265461,YAOGAN 21,1 40143U 14053A   19199.17328153  .00000433  00000-0  21144-4 0  9990,2 40143  97.4830 262.8236 0017957  11.2276  73.2654 15.23500207270351,YAOGAN 22,1 40275U 14063A   19199.14827992 -.00000045  00000-0  00000+0 0  9992,2 40275 100.4710 311.4575 0006658 139.4860 220.6733 13.15336132227681,ASNARO,1 40298U 14070A   19199.17093028  .00000413  00000-0  22600-4 0  9996,2 40298  97.4420 281.6280 0001771  74.9449 342.2286 15.19572133260429,HODOYOSHI-1,1 40299U 14070B   19199.07079981  .00000321  00000-0  16414-4 0  9992,2 40299  97.3489 278.2145 0011571 303.2435 129.2489 15.23386848260742,QSAT-EOS,1 40301U 14070D   19199.20744425  .00000818  00000-0  34419-4 0  9999,2 40301  97.3268 275.8228 0024373 296.4655 118.6247 15.25973804260792,YAOGAN 23,1 40305U 14071A   19199.14902689  .00000481  00000-0  27031-4 0  9993,2 40305  97.4922 136.2878 0001558 109.3250  14.9121 15.17919309258863,YAOGAN 24,1 40310U 14072A   19199.16332971  .00000018  00000-0  98080-5 0  9992,2 40310  97.8602 324.4096 0018378 125.3106 234.9842 14.76292656251069,CBERS 4,1 40336U 14079A   19199.16801060 -.00000034  00000-0  36275-5 0  9994,2 40336  98.6026 267.6335 0001357  68.6722 291.4612 14.35392161241588,RESURS P2,1 40360U 14087A   19199.20455670  .00000338  00000-0  12930-4 0  9994,2 40360  97.2590 289.3428 0008053 312.7205 110.0911 15.33144292254940,YAOGAN 26,1 40362U 14088A   19199.20491307  .00000399  00000-0  19505-4 0  9996,2 40362  97.2873 272.6356 0008233 224.4060 150.8377 15.23626192253826,SMAP,1 40376U 15003A   19199.14027259  .00000029  00000-0  14582-4 0  9993,2 40376  98.1226 205.6190 0000911  40.3734 319.7543 14.63376420238185,KOMPSAT-3A,1 40536U 15014A   19199.13315318 -.00000143  00000-0 -44848-5 0  9996,2 40536  97.5525 137.1645 0009764 213.1659 276.9316 15.12273307238045,SENTINEL-2A,1 40697U 15028A   19199.15862231 -.00000015  00000-0  11100-4 0  9993,2 40697  98.5663 273.2100 0000894  84.3722 275.7572 14.30817803212518,GAOFEN 8,1 40701U 15030A   19199.17978074  .00000501  00000-0  23936-4 0  9996,2 40701  97.4375 318.8813 0009509 243.6835 236.0820 15.23555358226198,CARBONITE 1 (CBNT-1),1 40718U 15032D   19199.15091010  .00000095  00000-0  21577-4 0  9997,2 40718  97.8822  84.9113 0015931 208.5279 151.5063 14.74707490216391,YAOGAN 27,1 40878U 15040A   19199.10303899 -.00000044  00000-0  00000+0 0  9997,2 40878 100.2857 252.5928 0007807 270.9422  89.0814 13.15962272186905,GAOFEN 9,1 40894U 15047A   19199.20016309 -.00000052  00000-0  00000+0 0  9999,2 40894  97.9167 280.7243 0034653  51.0384 309.3913 14.76286791207013,LAPAN-A2,1 40931U 15052B   19198.73032141  .00000451  00000-0 -26365-4 0  9996,2 40931   6.0013 209.5351 0012806 209.3049 150.7792 14.76607662205562,YAOGAN 28,1 41026U 15064A   19198.52517572  .00000149  00000-0  91771-5 0  9995,2 41026  97.4112 328.4858 0001443 140.2605 289.7606 15.23496142205942,YAOGAN 29,1 41038U 15069A   19199.19740055  .00000004  00000-0  73761-5 0  9990,2 41038  97.9777 183.3925 0000501  97.4969 262.6304 14.80412008196675,KENT RIDGE 1,1 41167U 15077B   19199.20065972  .00000715  00000-0  36214-5 0  9993,2 41167  14.9795  44.0807 0011231  57.4879 250.6784 15.09044209198056,TELEOS 1,1 41169U 15077D   19199.03535880  .00000918  00000-0  15107-4 0  9998,2 41169  14.9846  34.8310 0010507  71.9058 174.9283 15.10259460198125,GAOFEN 4,1 41194U 15083A   19198.76937278 -.00000345  00000-0  00000-0 0  9996,2 41194   0.0468 109.8985 0004022  91.2802 116.7534  1.00268209 13154,JASON-3,1 41240U 16002A   19198.86035638 -.00000057  00000-0  16156-4 0  9993,2 41240  66.0424 342.1943 0007927 269.2578  90.7529 12.80931082163573,SENTINEL-3A,1 41335U 16011A   19199.20854744  .00000003  00000-0  19191-4 0  9996,2 41335  98.6225 265.7620 0001432  93.2182 266.9162 14.26738033177872,SENTINEL-1B,1 41456U 16025A   19199.09269104 -.00000071  00000-0 -53360-5 0  9996,2 41456  98.1826 205.6925 0001210  90.0883 270.0455 14.59197661171828,ZIYUAN 3-2 (ZY 3-2),1 41556U 16033A   19199.19005134  .00000373  00000-0  19741-4 0  9995,2 41556  97.3970 275.9034 0003986  98.3873 313.8264 15.21295020173940,NUSAT-1 (FRESCO),1 41557U 16033B   19199.20066697  .00000715  00000-0  29501-4 0  9994,2 41557  97.4183 286.5298 0013599 186.7128 173.3931 15.27253114174590,NUSAT-2 (BATATA),1 41558U 16033C   19199.19839529  .00000761  00000-0  29515-4 0  9991,2 41558  97.4210 287.6692 0012623 186.8503 173.2567 15.29161021174644,CARTOSAT-2C,1 41599U 16040A   19199.18418201  .00000155  00000-0  10653-4 0  9993,2 41599  97.4974 257.2256 0012624 301.3932 121.9241 15.19228788170194,SKYSAT-C1,1 41601U 16040C   19199.18281672  .00000307  00000-0  15688-4 0  9999,2 41601  97.4067 277.4222 0001454  47.9023  35.9407 15.23694307170558,LAPAN-A3,1 41603U 16040E   19199.14226736  .00000130  00000-0  92543-5 0  9997,2 41603  97.3591 257.1264 0012118 300.0875 143.9902 15.19724579170212,BIROS,1 41604U 16040F   19198.13621169  .00000251  00000-0  14105-4 0  9996,2 41604  97.3627 258.7972 0010760 288.6846  71.3221 15.21738096170227,GAOFEN 3,1 41727U 16049A   19199.16835130 -.00000043  00000-0  19910-6 0  9991,2 41727  98.4124 206.4349 0000514  76.6073 283.5175 14.42214322154543,SKYSAT-C4,1 41771U 16058B   19198.79206698  .00000302  00000-0  15560-4 0  9991,2 41771  97.3996 269.5087 0001733  89.9464 270.1971 15.23541171157433,SKYSAT-C5,1 41772U 16058C   19199.15092767  .00000273  00000-0  14402-4 0  9994,2 41772  97.3987 269.4087 0001642  66.8822  16.7484 15.23396059157485,SKYSAT-C2,1 41773U 16058D   19199.16149161  .00000266  00000-0  14105-4 0  9999,2 41773  97.4017 270.0358 0001614  97.5480 262.5940 15.23423375157491,SKYSAT-C3,1 41774U 16058E   19198.91234400  .00000261  00000-0  13873-4 0  9999,2 41774  97.4027 269.7986 0002169  81.2430 278.9053 15.23490491157451,ALSAT 1B,1 41785U 16059C   19199.15714781 -.00000018  00000-0  59068-5 0  9992,2 41785  98.0632 258.4918 0030189 290.0070  69.7888 14.63322721149866,PATHFINDER 1,1 41787U 16059E   19199.14905396  .00000008  00000-0  10675-4 0  9995,2 41787  98.0633 259.1751 0029688 288.5995  71.1995 14.63675629149899,SCATSAT 1,1 41790U 16059H   19198.61579435  .00000087  00000-0  33135-4 0  9994,2 41790  98.2074 242.9988 0004410 225.4380 134.6425 14.50885396148522,WORLDVIEW-4 (WV-4),1 41848U 16067A   19198.68200547  .00001767  00000-0  10000-3 0  9990,2 41848  97.8602 280.0407 0022074 164.6040 195.5704 15.14281367145319,GOKTURK 1A,1 41875U 16073A   19199.15908548 -.00000005  00000-0  80781-5 0  9996,2 41875  98.1313  93.2905 0001166  76.5413 283.5924 14.62779530139549,RESOURCESAT-2A,1 41877U 16074A   19199.15433885 -.00000025  00000-0  81880-5 0  9999,2 41877  98.6320 271.3706 0000547  83.1028 277.0218 14.21663680135408,CARTOSAT-2D,1 41948U 17008A   19198.74909404  .00000317  00000-0  18207-4 0  9990,2 41948  97.3944 258.4819 0008280  87.2942 272.9256 15.19269903133994,SENTINEL-2B,1 42063U 17013A   19199.19362686 -.00000023  00000-0  78016-5 0  9990,2 42063  98.5667 273.2571 0001048  87.6443 272.4865 14.30817962123438,ZHUHAI-1 02 (CAS 4B),1 42759U 17034B   19199.12240141  .00000052  00000-0  16734-4 0  9998,2 42759  43.0198  69.1363 0010212  72.0956  62.5093 15.09598889115281,NUSAT-3 (MILANESAT),1 42760U 17034C   19199.12596330  .00000105  00000-0  19718-4 0  9998,2 42760  43.0182  69.0723 0008472  90.4865  45.0098 15.10031757115293,ZHUHAI-1 01 (CAS 4A),1 42761U 17034D   19199.08201772 -.00000022  00000-0  12215-4 0  9991,2 42761  43.0195  68.8767 0010691  72.5959  51.0492 15.09675439115276,CARTOSAT-2E,1 42767U 17036C   19198.96025801  .00000313  00000-0  18087-4 0  9997,2 42767  97.4701 257.4346 0009614 120.7115 239.5069 15.19227558114593,FORMOSAT-5,1 42920U 17049A   19198.71712286 -.00000045  00000-0  31096-7 0  9996,2 42920  98.3285 276.6759 0009505 282.0623  77.9506 14.50842596100333,SENTINEL-5P,1 42969U 17064A   19198.88612905 -.00000026  00000-0  85185-5 0  9996,2 42969  98.7368 137.9257 0001054  85.3802 274.7494 14.19542118 91141,SKYSAT-C11,1 42987U 17068A   19198.87835418  .00000280  00000-0  15386-4 0  9991,2 42987  97.4350 312.1746 0010218 111.1194 249.1135 15.21862701 94836,SKYSAT-C10,1 42988U 17068B   19199.12785186  .00000273  00000-0  15056-4 0  9993,2 42988  97.4343 312.3171 0005296 125.2600 234.9132 15.21847055 94879,SKYSAT-C9,1 42989U 17068C   19198.85527436  .00000266  00000-0  14731-4 0  9996,2 42989  97.4364 312.5171 0001815  98.0950 262.0492 15.21845729 94858,SKYSAT-C8,1 42990U 17068D   19199.10872660  .00000264  00000-0  14699-4 0  9990,2 42990  97.4374 312.6634 0004596 150.3437 209.8061 15.21759291 94889,SKYSAT-C7,1 42991U 17068E   19198.90038492  .00000274  00000-0  15163-4 0  9993,2 42991  97.4367 312.4676 0006208 164.4024 195.7405 15.21767419 94859,SKYSAT-C6,1 42992U 17068F   19198.75739529  .00000265  00000-0  14739-4 0  9994,2 42992  97.4369 312.5231 0010288 231.0694 128.9625 15.21796342 94845,CARTOSAT-2F,1 43111U 18004A   19199.20754831  .00000608  00000-0  32030-4 0  9998,2 43111  97.4326 260.2724 0005317 133.5488 281.9195 15.19255232 83814,MICROSAT-TD,1 43128U 18004T   19199.16724843  .00010956  00000-0  87487-4 0  9994,2 43128  96.8036 254.7792 0007357  19.5288 340.6256 15.71247558 86637,NUSAT-4 (ADA),1 43195U 18015D   19199.24857692  .00000664  00000-0  30435-4 0  9994,2 43195  97.3991 326.3812 0017094 192.2237 230.5301 15.23807898 80838,NUSAT-5 (MARYAM),1 43204U 18015K   19199.15423479  .00000629  00000-0  28821-4 0  9994,2 43204  97.3997 326.3732 0016762 194.5302 248.3817 15.24007761 80421,GAOFEN 1-02,1 43259U 18031A   19199.18612686  .00000012  00000-0  91101-5 0  9993,2 43259  97.9959 276.5225 0004402 173.7704 186.3572 14.76429982 69954,GAOFEN 1-03,1 43260U 18031B   19199.16350491  .00000010  00000-0  88185-5 0  9992,2 43260  97.9957 276.5268 0003924 265.8011  94.2759 14.76428074 69947,GAOFEN 1-04,1 43262U 18031D   19199.20860303  .00000052  00000-0  14824-4 0  9991,2 43262  97.9965 276.5117 0003648 184.2894 175.8298 14.76427953 69946,SENTINEL-3B,1 43437U 18039A   19198.62002607  .00000002  00000-0  18721-4 0  9998,2 43437  98.6284 265.1050 0001729  96.6652 263.4726 14.26735886 63854,GAOFEN 5,1 43461U 18043A   19199.18164797 -.00000025  00000-0  44434-5 0  9998,2 43461  98.1640 136.8940 0002450  29.3436 330.7903 14.57760887 63453,GAOFEN 6,1 43484U 18048A   19199.17640629 -.00000003  00000-0  69603-5 0  9991,2 43484  98.0167 277.0922 0010550  40.8156 319.3854 14.76482733 60644,GAOFEN 11,1 43585U 18063A   19199.19420568  .00000392  00000-0  19347-4 0  9999,2 43585  97.4127 278.5260 0017284 317.3066  94.4878 15.23567312 54199,AEOLUS,1 43600U 18066A   19199.05857105  .00017095  00000-0  67227-4 0  9994,2 43600  96.7153 205.4563 0002629  65.3587 329.3488 15.86955263 52187,ICESAT-2,1 43613U 18070A   19199.10942033  .00000726  00000-0  26191-4 0  9992,2 43613  92.0031 331.0273 0002909 103.7146 256.4427 15.28270666 46671,HYSIS,1 43719U 18096A   19199.18367203  .00000075  00000-0  17289-4 0  9990,2 43719  97.9463 266.4822 0009385 227.6109 132.4310 14.78651901 34122,KAZSTSAT,1 43783U 18099AB  19199.14902791  .00000330  00000-0  36672-4 0  9998,2 43783  97.7577 270.6557 0007936 211.4467 148.6279 14.93606233 33786,SAUDISAT 5A,1 43831U 18102A   19199.20376624 -.00000024  00000-0  25652-5 0  9992,2 43831  97.6046 274.5060 0013452 218.3677 199.0028 15.07902787 33614,SAUDISAT 5B,1 43833U 18102C   19199.21074146  .00000003  00000-0  43129-5 0  9994,2 43833  97.6042 274.6573 0014335 227.4576 219.1190 15.08340230 33616");
        String[] tles = sb.toString().split(",");
        String[] tlesFormat = new String[tles.length];
        for (int i = 0; i < tles.length; i++) {
            tlesFormat[i] = tles[i].trim();
        }
        //System.err.println(Arrays.toString(tlesFormat));

        //计算多个卫星星轨
        for (int i = 0; i < tlesFormat.length; i += 3) {
            //以OVS-1A星为例
            /*String[] tleStr = {
                    "ZHUHAI-1 01 (CAS 4A)",
                    "1 42761U 17034D   18093.84646991  .00000039  00000-0  16016-4 0  9993",
                    "2 42761  43.0189 131.1841 0009554 355.2876 341.2064 15.09309908 44209"
            };*/
            String[] tleStr = {
                    tlesFormat[i],
                    tlesFormat[i + 1],
                    tlesFormat[i + 2]
            };

            //实例化TLE
            TLE tle = new TLE(tleStr);
            //构建卫星
            Satellite satellite = SatelliteFactory.createSatellite(tle);
            // 如果不清楚基站位置，全设置为0也可
            GroundStationPosition groundStationPosition = new GroundStationPosition(0, 0, 0);
            // 第二个参数传入一个时间来确定卫星在该时刻的位置
            SatPos satPos = satellite.getPosition(groundStationPosition, new Date());

            // 自带格式化输出
            //System.out.println(tlesFormat[i] + "卫星当前信息：");
            //System.err.println(tlesFormat[i] + "卫星当前信息：\n" + satPos);

            System.out.println(satPos);
        }
    }
}