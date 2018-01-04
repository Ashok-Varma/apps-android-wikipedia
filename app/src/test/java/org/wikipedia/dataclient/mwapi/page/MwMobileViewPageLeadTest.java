package org.wikipedia.dataclient.mwapi.page;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wikipedia.dataclient.page.BasePageLeadTest;
import org.wikipedia.dataclient.page.PageClient;
import org.wikipedia.dataclient.page.PageLead;
import org.wikipedia.test.TestRunner;
import org.wikipedia.testlib.TestLatch;

import okhttp3.CacheControl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.wikipedia.json.GsonUnmarshaller.unmarshal;

@RunWith(TestRunner.class) public class MwMobileViewPageLeadTest extends BasePageLeadTest {
    private PageClient subject;

    @Before public void setUp() throws Throwable {
        super.setUp();
        subject = new MwPageClient(service(MwPageService.class));
    }

    @Test public void testEnglishMainPage() throws Exception {
        MwMobileViewPageLead pageLead = unmarshal(MwMobileViewPageLead.class, wrapInMobileview(getEnglishMainPageJson()));
        MwMobileViewPageLead.Mobileview props = pageLead.getMobileview();
        verifyEnglishMainPage(props);
    }


    @Test public void testUnprotectedDisambiguationPage() throws Exception {
        MwMobileViewPageLead pageLead = unmarshal(MwMobileViewPageLead.class,
                wrapInMobileview(getUnprotectedDisambiguationPageJson()));
        MwMobileViewPageLead.Mobileview props = pageLead.getMobileview();
        verifyUnprotectedDisambiguationPage(props);
    }

    /**
     * Custom deserializer; um, yeah /o\.
     * An earlier version had issues with protection settings that don't include "edit" protection.
     */
    @Test public void testProtectedButNoEditProtectionPage() throws Exception {
        MwMobileViewPageLead pageLead = unmarshal(MwMobileViewPageLead.class,
                wrapInMobileview(getProtectedButNoEditProtectionPageJson()));
        MwMobileViewPageLead.Mobileview props = pageLead.getMobileview();
        verifyProtectedNoEditProtectionPage(props);
    }

    @Test @SuppressWarnings("checkstyle:magicnumber") public void testThumbUrls() throws Throwable {
        enqueueFromFile("page_lead_mw.json");
        final TestLatch latch = new TestLatch();
        subject.lead(CacheControl.FORCE_NETWORK, PageClient.CacheOption.CACHE, "foo", 640)
                .enqueue(new Callback<PageLead>() {
                    @Override
                    public void onResponse(@NonNull Call<PageLead> call, @NonNull Response<PageLead> response) {
                        assertThat(response.body().getLeadImageUrl(640).contains("640px"), is(true));
                        assertThat(response.body().getThumbUrl().contains(preferredThumbSizeString()), is(true));
                        assertThat(response.body().getDescription(), is("Mexican boxer"));
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@NonNull Call<PageLead> call, @NonNull Throwable t) {
                        fail();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test public void testError() throws Exception {
        MwMobileViewPageLead pageLead = unmarshal(MwMobileViewPageLead.class, getErrorJson());
        MwMobileViewPageLead.Mobileview props = pageLead.getMobileview();
        verifyError(pageLead, props);
    }

    @NonNull @Override protected PageClient subject() {
        return subject;
    }

    private String wrapInMobileview(String json) {
        return "{\"mobileview\":" + json + "}";
    }
}
