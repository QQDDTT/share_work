package com.nick.share_work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareWorkApplication {

	public static void main(String[] args) {
		// 确保你的终端支持 ANSI 转义序列，以显示彩色输出。
		AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
		SpringApplication.run(ShareWorkApplication.class, args);
	}

}
