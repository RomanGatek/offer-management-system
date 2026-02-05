package com.example.offermanagementsystem;

import com.example.offermanagementsystem.repository.OfferRepository;
import com.example.offermanagementsystem.repository.UserRepository;
import com.example.offermanagementsystem.service.PdfExportService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
		properties = {
				"spring.autoconfigure.exclude=" +
						"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
						"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
		}
)
@SuppressWarnings("unused")
class OfferManagementSystemApplicationTests {

	@MockBean
	private OfferRepository offerRepository;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PdfExportService pdfExportService;

	@Test
	void contextLoads() {
		// OK – test projde, pokud Spring context naběhne
	}
}
