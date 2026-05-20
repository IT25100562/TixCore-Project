package com.ticketbooking.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.DottedLineSeparator;
import com.lowagie.text.pdf.draw.LineSeparator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Event;
import com.ticketbooking.model.User;
import com.ticketbooking.model.Venue;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.VenueRepository;
import com.ticketbooking.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/bookings")
public class TicketPdfController {

    @Autowired private BookingRepository bookingRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private VenueRepository venueRepo;

    private static final Color BRAND      = new Color(124, 58, 237);
    private static final Color BRAND_DARK = new Color(76, 29, 149);
    private static final Color INK        = new Color(17, 24, 39);
    private static final Color MUTED      = new Color(107, 114, 128);
    private static final Color SOFT_BG    = new Color(245, 243, 255);
    private static final Color HAIRLINE   = new Color(229, 231, 235);

    @GetMapping("/{id}/ticket")
    public ResponseEntity<?> downloadTicket(@PathVariable String id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).build();
        String userId = (String) session.getAttribute(SessionUtil.USER_ID);
        boolean isAdmin = SessionUtil.isAdmin(session);

        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        if (!isAdmin && !userId.equals(b.getUserId())) return ResponseEntity.status(403).build();

        Event ev = b.getEventId() != null ? eventRepo.findById(b.getEventId()).orElse(null) : null;
        User u  = b.getUserId()  != null ? userRepo.findById(b.getUserId()).orElse(null)   : null;
        Venue venue = (ev != null && ev.getVenueId() != null) ? venueRepo.findById(ev.getVenueId()).orElse(null) : null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 56, 56, 50, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new BorderEvent());
            doc.open();

            // ----- Header bar (full width, branded) -----
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell brandCell = new PdfPCell(new Phrase("TIXCORE CINEMA",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.WHITE)));
            brandCell.setBackgroundColor(BRAND);
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.setPadding(16);
            brandCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(brandCell);

            PdfPCell subCell = new PdfPCell(new Phrase("ADMIT ONE  •  E-TICKET",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_DARK)));
            subCell.setBackgroundColor(SOFT_BG);
            subCell.setBorder(Rectangle.NO_BORDER);
            subCell.setPaddingTop(7);
            subCell.setPaddingBottom(7);
            subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(subCell);
            doc.add(header);

            // ----- Movie title -----
            Paragraph movie = new Paragraph(ev != null ? ev.getTitle() : "Event",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, INK));
            movie.setAlignment(Element.ALIGN_CENTER);
            movie.setSpacingBefore(14);
            doc.add(movie);

            if (ev != null && ev.getCategory() != null && !ev.getCategory().isBlank()) {
                Paragraph cat = new Paragraph(ev.getCategory().toUpperCase(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND));
                cat.setAlignment(Element.ALIGN_CENTER);
                cat.setSpacingAfter(6);
                doc.add(cat);
            }

            doc.add(hairline());

            // ----- Movie Details -----
            doc.add(sectionTitle("MOVIE DETAILS"));
            PdfPTable movieTbl = twoColTable();
            addRow(movieTbl, "Movie",  ev != null ? ev.getTitle() : "—");
            addRow(movieTbl, "Screen", "Screen " + screenNumberFor(b.getId()));
            addRow(movieTbl, "Date",   ev != null ? ev.getDate() : "—");
            addRow(movieTbl, "Time",   ev != null ? ev.getTime() : "—");
            addRow(movieTbl, "Venue",  venue != null
                    ? venue.getName() + (venue.getCity() != null && !venue.getCity().isBlank() ? ", " + venue.getCity() : "")
                    : "—");
            doc.add(movieTbl);

            doc.add(hairline());

            // ----- Booking Details -----
            doc.add(sectionTitle("BOOKING DETAILS"));
            PdfPTable bookTbl = twoColTable();
            addRow(bookTbl, "Booking ID", b.getId());
            addRow(bookTbl, "Name",       u != null ? (notBlank(u.getFullName()) ? u.getFullName() : u.getUsername()) : "—");
            addRow(bookTbl, "Seats",      notBlank(b.getSeats()) ? b.getSeats() : "—");
            addRow(bookTbl, "Status",     notBlank(b.getStatus()) ? b.getStatus().toUpperCase() : "—");
            addRow(bookTbl, "Total",      "Rs. " + formatPrice(b.getTotalPrice()));
            doc.add(bookTbl);

            // ----- Dashed tear-line -----
            Paragraph spacerBefore = new Paragraph(" ");
            spacerBefore.setSpacingBefore(8);
            doc.add(spacerBefore);
            DottedLineSeparator dotted = new DottedLineSeparator();
            dotted.setLineColor(BRAND);
            dotted.setLineWidth(1.2f);
            dotted.setGap(4f);
            Paragraph tearLine = new Paragraph(new Chunk(dotted));
            tearLine.setSpacingAfter(8);
            doc.add(tearLine);

            // ----- QR Code (centered) -----
            try {
                Image qrImg = Image.getInstance(generateQrPng(b.getId(), 240));
                qrImg.scaleAbsolute(110, 110);
                qrImg.setAlignment(Image.ALIGN_CENTER);
                doc.add(qrImg);

                Paragraph scan = new Paragraph("Scan at the entrance",
                        FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED));
                scan.setAlignment(Element.ALIGN_CENTER);
                scan.setSpacingBefore(3);
                doc.add(scan);
            } catch (Exception qrEx) {
                Paragraph fallback = new Paragraph("Booking ref: " + b.getId(),
                        FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED));
                fallback.setAlignment(Element.ALIGN_CENTER);
                doc.add(fallback);
            }

            // ----- Footer -----
            Paragraph foot = new Paragraph("Enjoy Your Movie!",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND_DARK));
            foot.setAlignment(Element.ALIGN_CENTER);
            foot.setSpacingBefore(10);
            doc.add(foot);

            Paragraph footSub = new Paragraph(
                    "This ticket is non-transferable. Please arrive 15 minutes before showtime.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, MUTED));
            footSub.setAlignment(Element.ALIGN_CENTER);
            footSub.setSpacingBefore(2);
            doc.add(footSub);

            doc.close();
            byte[] pdf = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket-" + b.getId() + ".pdf");
            headers.setContentLength(pdf.length);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return ResponseEntity.status(500).headers(headers).body(("PDF error: " + e.getMessage()).getBytes());
        }
    }

    /** Draws a brand-coloured outer border on every page. */
    private static class BorderEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            Rectangle pageSize = document.getPageSize();
            float margin = 30f;
            cb.saveState();
            cb.setColorStroke(BRAND);
            cb.setLineWidth(1.6f);
            cb.rectangle(margin, margin,
                    pageSize.getWidth()  - 2 * margin,
                    pageSize.getHeight() - 2 * margin);
            cb.stroke();
            cb.restoreState();
        }
    }

    // ============== helpers ==============

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private static String formatPrice(double v) {
        return String.format("%,.2f", v);
    }

    private static byte[] generateQrPng(String payload, int sizePx) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);
        BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }

    private static int screenNumberFor(String bookingId) {
        if (bookingId == null || bookingId.isEmpty()) return 1;
        int h = 0;
        for (int i = 0; i < bookingId.length(); i++) h = (h * 31 + bookingId.charAt(i)) & 0x7fffffff;
        return (h % 8) + 1;
    }

    private static PdfPTable twoColTable() {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{1.0f, 1.7f}); } catch (Exception ignored) {}
        t.setSpacingBefore(2);
        t.setSpacingAfter(2);
        return t;
    }

    private static void addRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label.toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, MUTED)));
        PdfPCell v = new PdfPCell(new Phrase(value == null ? "" : value,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, INK)));
        l.setBorder(Rectangle.NO_BORDER); v.setBorder(Rectangle.NO_BORDER);
        l.setPaddingTop(5); l.setPaddingBottom(5); l.setPaddingLeft(2); l.setPaddingRight(6);
        v.setPaddingTop(5); v.setPaddingBottom(5); v.setPaddingLeft(2); v.setPaddingRight(2);
        l.setVerticalAlignment(Element.ALIGN_MIDDLE);
        v.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(l); table.addCell(v);
    }

    private static Paragraph sectionTitle(String txt) {
        Paragraph p = new Paragraph(txt,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND));
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        return p;
    }

    private static Paragraph hairline() {
        LineSeparator ls = new LineSeparator(0.7f, 100, HAIRLINE, Element.ALIGN_CENTER, 0);
        Paragraph p = new Paragraph(new Chunk(ls));
        p.setSpacingBefore(6);
        p.setSpacingAfter(2);
        return p;
    }
}
