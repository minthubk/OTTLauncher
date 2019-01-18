package com.pisen.ott.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 天气详细信息对象
 * (初步定义天气信息为一天的简要信息,后期需扩展参考文档SmartWeatherAPI_Lite_WebAPI_3.0.2)
 * @author Liuhc
 * @version 1.0 2014年12月8日 下午2:23:59
 */
public class WeatherInfo implements Parcelable{
	
	//天气所属城市区域id
	public String areaid;
	//城市名
	public String cityName;
	//天气更新时间
	public String releaseTime;
	//白天天气编号
	public String dayWeatherNum;
	//白天天气
	public String dayWeather;
	//白天天气温度
	public String dayTemp;
	//夜间天气编号
	public String nightWeatherNum;
	//夜间天气
	public String nightWeather;
	//夜间天气温度
	public String nightTemp;
	

	public WeatherInfo() {}
	

	public String getAreaid() {
		return areaid;
	}

	public void setAreaid(String areaid) {
		this.areaid = areaid;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(String releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getDayWeatherNum() {
		return dayWeatherNum;
	}

	public void setDayWeatherNum(String dayWeatherNum) {
		this.dayWeatherNum = dayWeatherNum;
	}

	public String getDayWeather() {
		return dayWeather;
	}

	public void setDayWeather(String dayWeather) {
		this.dayWeather = dayWeather;
	}

	public String getDayTemp() {
		return dayTemp;
	}

	public void setDayTemp(String dayTemp) {
		this.dayTemp = dayTemp;
	}

	public String getNightWeatherNum() {
		return nightWeatherNum;
	}

	public void setNightWeatherNum(String nightWeatherNum) {
		this.nightWeatherNum = nightWeatherNum;
	}

	public String getNightWeather() {
		return nightWeather;
	}

	public void setNightWeather(String nightWeather) {
		this.nightWeather = nightWeather;
	}

	public String getNightTemp() {
		return nightTemp;
	}

	public void setNightTemp(String nightTemp) {
		this.nightTemp = nightTemp;
	}

	@Override
	public String toString() {
		return "WeatherInfo [areaid=" + areaid + ", cityName=" + cityName + ", releaseTime=" + releaseTime + ", dayWeatherNum=" + dayWeatherNum
				+ ", dayWeather=" + dayWeather + ", dayTemp=" + dayTemp + ", nightWeatherNum=" + nightWeatherNum + ", nightWeather=" + nightWeather
				+ ", nightTemp=" + nightTemp + "]";
	}

	
	
	public static final Parcelable.Creator<WeatherInfo> CREATOR = new Creator<WeatherInfo>() {
		
		@Override
		public WeatherInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new WeatherInfo[size];
		}
		
		@Override
		public WeatherInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new WeatherInfo(source);
		}
	};
	
	/**
	 * @param source
	 */
	public WeatherInfo(Parcel source) {
		readFromParcel(source);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	//注意写入变量和读取变量的顺序应该一致 不然得不到正确的结果 
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(areaid);
		dest.writeString(cityName);
		dest.writeString(releaseTime);
		dest.writeString(dayWeatherNum);
		dest.writeString(dayWeather);
		dest.writeString(dayTemp);
		dest.writeString(nightWeatherNum);
		dest.writeString(nightWeather);
		dest.writeString(nightTemp);
        
	}
	
	 //注意读取变量和写入变量的顺序应该一致 不然得不到正确的结果  
    public void readFromParcel(Parcel source) {
    	areaid = source.readString();
    	cityName = source.readString();
    	releaseTime = source.readString();
    	dayWeatherNum = source.readString();
    	dayWeather = source.readString();
    	dayTemp = source.readString();
    	nightWeatherNum = source.readString();
    	nightWeather = source.readString();
    	nightTemp = source.readString();
    }
	
}
