/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testcrawlermotor;

import bib.base.Base;
import bib.crawler.Crawler;
import bib.web.Link;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;

/**
 *
 * @author Rafael
 */
class MotorCrawler extends Base {

    void run() {
        ArrayList<String> allLinks = new ArrayList<>();
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__518__517__Electric_Motors-Micro_Below_22mm_.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=518&curPage=2&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__1219__517__Electric_Motors-22_to_27mm.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__520__517__Electric_Motors-28_to_34mm.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__1217__517__Electric_Motors-35_to_44mm.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__522__517__Electric_Motors-45_to_50mm.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__1218__517__Electric_Motors-Above_50mm.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__519__517__Electric_Motors-Bell_Type.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/__523__517__Electric_Motors-Specialised_Motors.html");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=520&curPage=2&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=520&curPage=3&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=1217&curPage=2&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=1217&curPage=3&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=522&curPage=2&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");
        allLinks.add("http://www.hobbyking.com/hobbyking/store/uh_listCategoriesAndProducts.asp?cwhl=XX&pc=517&idCategory=523&curPage=2&v=&sortlist=&sortMotor=&LiPoConfig=&CatSortOrder=desc");

        for (String allLink : allLinks) {
            System.out.println(allLink);
            Crawler crawler = new Crawler();
            crawler.readPage(allLink);
            ArrayList<Link> links = crawler.getAllLinks("www.hobbyking.com/hobbyking/store");
            for (int i = 0; i < links.size(); i++) {
                if (links.get(i).getUrl().contains("idproduct")) {
                    Crawler crawlerProd = new Crawler();
                    crawlerProd.readPage(links.get(i).getUrl());
                    Element data = crawlerProd.getElementOfPage("class", "data_tbl");
                    if (data != null) {
                        String dataTable = data.text();
                        String atributes = this.getDataProductConfigTable(dataTable);
                        atributes += this.getSpecificFeatures(crawlerProd, links.get(i).getUrl());
                        log.writeFile("reportexcelappend", atributes);
                    }
                }
            }
        }
    }

    private String getSpecificFeatures(Crawler crawlerProd, String url) {
        String result = "";
        String details = crawlerProd.getElementOfPage("itemprop", "name").text();
        result += "\"" + details + "\";";
        details = crawlerProd.getElementOfPage("id", "productDetails").text().replace("\"", "'").replace("\n", "").replace("\r", "");
        result += "\"" + details + "\";";
        details = crawlerProd.getElementOfPage("itemprop", "price").text().replace("R$", "").replace(".", ",");
        result += "\"" + details + "\";";
        details = url;
        result += "\"" + details + "\";";
        String[] productDetails = crawlerProd.getElementOfPage("id", "productDetails").html().toLowerCase().split("<br />");
        String thrust = "";
        for (String detail : productDetails) {
            if (detail.contains("thrust")) {
                ArrayList<String> patternsList = new ArrayList<>();
                patternsList.add("( +)([0-9]+g)");
                thrust = this.getAllValueRegex(detail, patternsList);
                break;
            }
        }
        result += thrust;
        return result;
    }

    private String getDataProductConfigTable(String details) {
        details = details.replace("(", "");
        details = details.replace(")", "");
        ArrayList<String> patternsList = new ArrayList<>();
        patternsList.add("(Weight g )(\\d+)");
        patternsList.add("(Shaft A mm )(\\d+)");
        patternsList.add("(Kvrpm/v )(\\d+)");
        patternsList.add("(Length B mm )(\\d+)");
        patternsList.add("(Diameter C mm )(\\d+)");
        patternsList.add("(Can Length mm )(\\d+)");
        patternsList.add("(Total Length E mm )(\\d+)");
        patternsList.add("(Max CurrentA )(\\d+|\\d+\\.\\d+)");
        patternsList.add("(Resistancemh )(\\d+)");
        patternsList.add("(Max VoltageV )(\\d+)");
        patternsList.add("(PowerW )(\\d+)");

        return this.getAllValueRegex(details, patternsList);
    }

    private String getAllValueRegex(String text, ArrayList<String> patternsList) {
        String values = "";
        for (String pattern : patternsList) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                values += "\"" + m.group(2) + "\";";
            } else {
                values += "\"" + "\";";

            }
        }
        return values;

    }

    private String getValueRegex(String text, String pattern, String key) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return key + ":" + m.group(2);
        } else {
            return "";
        }
    }

}
