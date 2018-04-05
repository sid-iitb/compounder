package noob.sk4x0r.compounder;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Scrapper {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> symbols = Arrays.asList(new String[]{"MI42", "PMS01", "AI45", "AO04", "ABB", "ACC06", "AE13", "MPS", "AP11", "AT22", "PFR", "AET", "AL9", "ACI12", "AIE01", "AP22", "ICI", "AP35", "AL05", "AB15", "AGL02", "ARB", "AC18", "AA01", "ARI", "AB14", "AI40", "BT09", "AHE", "AT14", "A18", "AIG01", "AL", "AB", "AP31", "APT02", "AZP", "A06", "AP", "AF21", "AB16", "BA10", "BC02", "BE", "BAF", "BF04", "BH06", "BHI", "BI03", "BI12", "BLC", "BCM", "BOB", "BOI", "BAS", "BI01", "BEM03", "BPI02", "BFU", "BES", "BE03", "SM11", "BF03", "BA08", "BI14", "BHE", "BS14", "BL03", "BC07", "BGV", "BDE", "BS", "BBT", "BDM", "B05", "BPC", "BI", "CHC", "CFH", "CB06", "FCH", "CPL", "CU", "CAR", "CI01", "CC10", "C07", "CBO01", "CTI", "CP9", "CS18", "CES", "CGC01", "CG", "CFC", "CPC02", "CDB", "C", "CUB", "CI11", "CDE", "CPI", "CCI", "CI45", "CB", "CK", "CRI", "CI02", "IE07", "DI", "DBE", "C13", "DR", "DCB01", "DCM02", "DFP", "DC11", "DN02", "DB", "DHF", "DA01", "DB04", "DTV", "DL03", "D04", "DLP01", "DRL", "DCI", "eS06", "EC01", "EM", "EID", "EIH", "E06", "ET01", "EI14", "EH03", "EIM", "E", "EP11", "EII02", "EI", "FDC", "FB", "FMG", "FC01", "FI", "FS07", "FH", "FVI", "FR", "GAI", "GD01", "G04", "GP10", "AP29", "GES", "ATD", "GI22", "GSC04", "GSK", "GP08", "GI27", "GNV", "GPI", "GCP", "GI23", "GP11", "GI25", "GC20", "GI19", "GN", "GRU", "GSF", "GAC01", "GF07", "GHC", "GMD", "GSP02", "GGC", "GPP03", "GOL01", "GVK", "HCD", "HI01", "HCL", "HCL02", "HDF", "HDF01", "HDI", "HCI02", "HHM", "HT02", "HFC", "HS", "HCC", "HC07", "HZ", "HI", "HMV", "HA04", "HPC", "HSI02", "HT", "HU", "ICI02", "IPL01", "ICR", "IDB05", "IC8", "IDF", "IDF01", "IFC02", "IM01", "IG04", "ITN", "IC", "IHF01", "IRE01", "IB04", "IHC", "ICI07", "IR05", "IIB", "IIL03", "IEI01", "IT", "INO01", "IW", "IDA", "IA04", "IOB", "IOC", "IL", "IID01", "ITC", "ITD03", "KI01", "JKC03", "JP12", "JC08", "JIS02", "JA02", "JBC01", "JBF", "JA01", "JPF01", "JSP", "JKB", "JKL01", "JKT01", "JMF", "JMT02", "HHL", "JE01", "JSW01", "JF04", "JO03", "JD", "JL", "KC06", "KPT", "KNP", "KB04", "KVB", "KSC01", "KEC04", "KI08", "KG01", "KC13", "KD07", "KMB", "KPI02", "M15", "KRB01", "KDI01", "LFH", "LI12", "LTS", "LMW", "LVB", "LT", "LL05", "LIC", "L", "MM", "MMF04", "MF20", "MG02", "MF19", "MHR", "MGF01", "MB04", "M13", "MP21", "MS24", "MIL16", "MRI02", "M12", "MC23", "MI4", "MT13", "MMT", "M18", "MI39", "MSS01", "MOF01", "MB02", "MRF", "MRP", "MTN", "MF10", "NAC", "NH", "NP07", "NBV", "NFI", "NC02", "NP01", "NBC01", "NCC01", "NMI", "N07", "NII02", "NP08", "NFP01", "NLC", "NMD02", "NTP", "OR", "OI13", "O02", "ONG", "OFS01", "OC10", "OBC", "OMD", "PGH", "PI35", "PMF01", "PJ", "PS15", "PLN", "P", "PM02", "PII", "PI11", "PH05", "PNB05", "PHF", "PI26", "PSL01", "PFC02", "PGC", "PI17", "PEP02", "PC", "PTC02", "PIF02", "PL9", "PVR", "QC", "RK01", "RC12", "RE07", "RI03", "MC", "RS17", "RF17", "RCF01", "RMT", "IP09", "R", "RB03", "REC02", "RI37", "RC", "RII", "RF07", "RI", "RC13", "RI38", "PS04", "RP", "RE09", "RHF", "RI15", "RSI", "KC17", "SE19", "SAI", "AP26", "SBI", "FAG", "SEI04", "SC04", "SF14", "SM19", "SCT02", "SCI", "SC12", "SRS03", "SCU", "STF", "S", "S11", "SKF01", "SM", "SL17", "SD6", "SII04", "SC49", "SS42", "SIB", "SRE02", "SRF", "STC", "ST20", "SA10", "SCI08", "SPI", "SPA", "STN01", "SF20", "SF23", "SR05", "SI48", "SLS01", "SE17", "SM09", "SCS04", "SB9", "SI10", "TS09", "TNN", "TC", "TC14", "TC17", "TE", "TT", "TIC", "TM03", "TMD", "TPC", "TSI", "TIS", "TCS", "TM4", "TEE", "TRE", "T", "TCI", "TT16", "TWO", "TI23", "TI01", "TP06", "TP14", "T04", "AI01", "TT14", "TTK02", "TVT", "IBN", "TVS", "TVS03", "UCO", "U01", "UFS01", "UTC01", "UL02", "UBI01", "U", "UB02", "US", "UP04", "VI02", "VTW", "VS", "VT10", "VB05", "SG", "VLF", "VB03", "VO01", "VIP", "V", "VL03", "VST", "WAB", "WGS", "WI03", "WI", "W", "W05", "WH01", "YB", "ZEE", "ZL01", "ZT02", "ZW01"});

        for(String symbol:symbols){
                    Scrapper.printData(symbol);
        }
    }
    public static void printData(String symbol) throws IOException {
        for (int page = 1; page < 25; page++) {
            Document doc = Jsoup.connect("http://www.moneycontrol.com/stocks/hist_stock_result.php?ex=N&sc_id=" + symbol + "&pno="+page+"&hdn=daily&fdt=2000-01-01&todt=2018-08-09").get();
            if(doc.toString().contains("No data to display.")){
                return;
            }
            Elements symbolClass = doc.getElementsByClass("PT15");
            Elements spans = symbolClass.select("span");
            String scriptName = null;
            try {

                for (Element span : spans) {
                    if (span.text().contains("NSE:"))
                        scriptName = span.text().split(" ")[1];
                }
            }catch (ArrayIndexOutOfBoundsException e){
                return;
            }

            Elements table = doc.getElementsByClass("tblchart");
            Elements rows = table.select("tr");
            for (int i = 2; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");
                log.info(scriptName + "\t");
                System.out.print(scriptName + "\t");
                for (int j = 0; j < cols.size() - 2; j++) {
                    log.info(cols.get(j).text() + "\t");
                    System.out.print(cols.get(j).text() + "\t");
                }
                System.out.println();
            }
        }
    }
}
