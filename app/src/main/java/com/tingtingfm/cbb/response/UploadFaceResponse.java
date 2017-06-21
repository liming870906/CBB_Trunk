package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;


public class UploadFaceResponse extends BaseResponse {
	public Face data;
	
	public class Face{
		private int id; //返回所属类型的id，0表示没有
		private String url; //访问地址
		private String location_type; //所属类型

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getLocation_type() {
			return location_type;
		}

		public void setLocation_type(String location_type) {
			this.location_type = location_type;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "Face{" +
					"id=" + id +
					", url='" + url + '\'' +
					", location_type='" + location_type + '\'' +
					'}';
		}
	}
}
