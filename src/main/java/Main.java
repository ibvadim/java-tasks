import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


//7dc44a1aeae654027a41fe4df2492e1a OpenWeatherMap
//34269f5fc5f933ce2e39471a647271b4 WeatherStack

public class Main {
    //Запрос к api по url и получение ответа
    private static String getUrlContent(String urlAdress) {
        StringBuffer content = new StringBuffer();

        try {
            URL url = new URL(urlAdress);
            URLConnection urlConn = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null){
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e){
            System.out.println("City not found");
        }
        return content.toString();
    }

    //Получаем погоду с сервиса OpenWeatherMap по api
    private static String openWeatherMap(String city) {
        String weatherData = getUrlContent("http://api.openweathermap.org/data/2.5/weather?q="+ city +"&appid=7dc44a1aeae654027a41fe4df2492e1a&units=metric");
        String output = "No data";
        if (!weatherData.isEmpty()){
            JSONObject obj = new JSONObject(weatherData);
            output = city + "\n";
            output += "Temperature: " + obj.getJSONObject("main").getDouble("temp") + "°C\n";
            output += "Feels like: " + obj.getJSONObject("main").getDouble("feels_like") + "°C\n";
            output += "Humidity: " + obj.getJSONObject("main").getDouble("humidity") + "%\n";
            output += "Wind speed: " + obj.getJSONObject("wind").getDouble("speed") + "m/s\n";
        }
        return output;
    }
    //Получаем погоду с сервиса WeatherStack по api
    private static String weatherStack(String city) {

        String weatherData = getUrlContent("http://api.weatherstack.com/current?access_key=34269f5fc5f933ce2e39471a647271b4&query=" + city);
        String output = "No data";
        if (!weatherData.isEmpty()){
            JSONObject obj = new JSONObject(weatherData);
            output = city + "\n";
            output += "Temperature: " + obj.getJSONObject("current").getDouble("temperature") + "°C\n";
            output += "Feels like: " + obj.getJSONObject("current").getDouble("feelslike") + "°C\n";
            output += "Humidity: " + obj.getJSONObject("current").getDouble("humidity") + "%\n";
            output += "Wind speed: " + obj.getJSONObject("current").getDouble("wind_speed") + "km/h\n";
        }
        return output;
    }

    //Вывод главного меню
    static void mainMenu(){
        System.out.println("Main menu:");
        System.out.println("1.Select a weather forecast source\n2.Select default city\n3.Show default settings\n4.Show weather in default city\n5.Show weather in another city\n6.Exit");
    }

    // Сохранение в файл WeatherAppSave.json
    // {"Source": "OpenWeatherMap", "City": "Moscow"}
    static void saving(String city, String source){
        JSONObject obj = new JSONObject();
        obj.put("City", city);
        obj.put("Source", source);
        try(FileWriter writer = new FileWriter("WeatherAppSave.json", false))
        {
            writer.write(obj.toString());
            writer.flush();
        }
        catch(IOException e){
            System.out.println("Writing save file error");
        }
    }
    //Чтение файла сохранения в String
    private static String readUsingFiles(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }


    public static void main(String[] args) throws IOException {
        Scanner command = new Scanner(System.in);
        String defaultSource = "OpenWeatherMap"; //Источник по умолчанию
        String defaultCity = "Moscow"; //Город по умолчанию

        File saveFile = new File("WeatherAppSave.json");
        if (saveFile.exists()) { //Если найден файл сохранения, то пытаемся его прочитать
            try {
                String saveData = readUsingFiles("WeatherAppSave.json");
                JSONObject obj = new JSONObject(saveData);
                defaultSource = obj.getString("Source"); //Перезаписываем источник
                defaultCity = obj.getString("City"); //Перезаписываем город
            } catch (Exception e) { //Если что то не так с файлом, выводим ошибку и используем данные по умолчанию
                System.out.println("Save import error");
            }

        }

        mainMenu(); //выводим главное меню
        boolean running = true;
        boolean selection;
        String inputData;

        while(running){
            switch(command.nextInt()){

                case 1: //Выбор сервиса для получения погоды
                    System.out.println(defaultSource + " is current default");
                    System.out.println("-----------------------------------");
                    System.out.println("1.OpenWeatherMap\n2.WeatherStack\n4.Back");
                    selection = true;
                    while(selection){
                        switch (command.nextInt()) {
                            case 1:
                                defaultSource = "OpenWeatherMap";
                                System.out.println(defaultSource + " set as default");
                                saving(defaultCity, defaultSource);
                                selection = false;
                                break;
                            case 2:
                                defaultSource = "WeatherStack";
                                System.out.println(defaultSource + " set as default");
                                saving(defaultCity, defaultSource);
                                selection = false;
                                break;
                            case 4:
                                selection = false;
                                break;
                            default:
                                System.out.println("Try again");
                                break;
                        }
                    }

                    mainMenu();
                    break;

                case 2: //Выбор города по умолчанию
                    System.out.println(defaultCity + " is current default");
                    System.out.println("-----------------------------------");
                    System.out.println("Enter city\n1.Back");
                    command.nextLine();
                    inputData = command.nextLine();
                    if (!(inputData).equals("1")){ //Если получили название города, а не 1(Назад)
                        defaultCity = inputData;
                        System.out.println(defaultCity + " set as default");
                        saving(defaultCity, defaultSource);
                    }
                    mainMenu();
                    break;

                case 3: //Вывод города и сервиса по умолчанию
                    System.out.println("Default city: " + defaultCity);
                    System.out.println("Default weather forecast source: " + defaultSource);
                    break;

                case 4: //Вывод информации о погоде используя данные по умолчанию
                    if(defaultSource.equals("OpenWeatherMap")){
                        System.out.println(openWeatherMap(defaultCity));
                    }
                    if(defaultSource.equals("WeatherStack")){
                        System.out.println(weatherStack(defaultCity));
                    }
                    break;

                case 5: //Вывод необходимого города использя сервис по умолчанию
                    System.out.println("Enter city\n1.Back");
                    command.nextLine();
                    inputData = command.nextLine();
                    if (!(inputData).equals("1")){
                        if(defaultSource.equals("OpenWeatherMap")){
                            System.out.println(openWeatherMap(inputData));
                        }
                        if(defaultSource.equals("WeatherStack")){
                            System.out.println(weatherStack(inputData));
                        }
                    }
                    mainMenu();
                    break;

                case 6: //Завершение программы
                    System.out.println("Bye");
                    running = false;
                    break;

                default: //Если ввели что-то непонятное
                    System.out.println("Try again");
                    break;
            }
        }
        command.close();
    }
}
