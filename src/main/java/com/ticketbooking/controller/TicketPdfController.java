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

@RestController // Indicates this controller returns data directly (like PDF bytes), not HTML views.
@RequestMapping("/api/bookings") // Base API route for these endpoints.
public class TicketPdfController {

    //  RELATIONSHIPS: Dependencies injected to fetch required data for the PDF 
    @Autowired private BookingRepository bookingRepo; 
    @Autowired private EventRepository eventRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private VenueRepository venueRepo;

    // Defines constant colors used for styling the PDF UI.
    private static final Color BRAND      = new Color(124, 58, 237);
    private static final Color BRAND_DARK = new Color(76, 29, 149);
    private static final Color INK        = new Color(17, 24, 39);
    private static final Color MUTED      = new Color(107, 114, 128);
    private static final Color SOFT_BG    = new Color(245, 243, 255);
    private static final Color HAIRLINE   = new Color(229, 231, 235);

    @GetMapping("/{id}/ticket") // Handles GET requests to download a specific ticket PDF.
    public ResponseEntity<?> downloadTicket(@PathVariable String id, HttpSession session) {
        // VALIDATION & SECURITY: Checks if the user is authenticated in the current session.
        if (!SessionUtil.isLoggedIn(session)) return ResponseEntity.status(401).build(); // 401 Unauthorized
        String userId = (String) session.getAttribute(SessionUtil.USER_ID); // Gets logged in user ID.
        boolean isAdmin = SessionUtil.isAdmin(session); // Checks if user has admin privileges.

        Booking b = bookingRepo.findById(id).orElse(null); // Fetches the booking from text file DB.
        if (b == null) return ResponseEntity.notFound().build(); // ERROR HANDLING: Returns 404 if booking not found.
        // SECURITY: If not admin and the booking doesn't belong to the user, deny access (403 Forbidden).
        if (!isAdmin && !userId.equals(b.getUserId())) return ResponseEntity.status(403).build();

        // Fetches related Event, User, and Venue models to populate the PDF content.
        Event ev = b.getEventId() != null ? eventRepo.findById(b.getEventId()).orElse(null) : null;
        User u  = b.getUserId()  != null ? userRepo.findById(b.getUserId()).orElse(null)   : null;
        Venue venue = (ev != null && ev.getVenueId() != null) ? venueRepo.findById(ev.getVenueId()).orElse(null) : null;

        try { // ERROR HANDLING: Try block catches any issues during PDF generation.
            ByteArrayOutputStream out = new ByteArrayOutputStream(); // Stream to hold PDF data in memory.
            Document doc = new Document(PageSize.A4, 56, 56, 50, 40); // Creates A4 document with margins.
            PdfWriter writer = PdfWriter.getInstance(doc, out); // Attaches PDF writer to document.
            writer.setPageEvent(new BorderEvent()); // Adds custom page border event.
            doc.open(); // Opens document for writing.

            //  Header bar (full width, branded) 
            PdfPTable header = new PdfPTable(1); // Single column table for header.
            header.setWidthPercentage(100);
            PdfPCell brandCell = new PdfPCell(new Phrase("TIXCORE CINEMA", // Adds main brand title.
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.WHITE)));
            brandCell.setBackgroundColor(BRAND); // Applies styling to cell.
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.setPadding(16);
            brandCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(brandCell);

            PdfPCell subCell = new PdfPCell(new Phrase("ADMIT ONE  •  E-TICKET", // Adds subtitle.
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_DARK)));
            subCell.setBackgroundColor(SOFT_BG);
            subCell.setBorder(Rectangle.NO_BORDER);
            subCell.setPaddingTop(7);
            subCell.setPaddingBottom(7);
            subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(subCell);
            doc.add(header); // Adds completed header table to the PDF document.

            //  Movie title 
            Paragraph movie = new Paragraph(ev != null ? ev.getTitle() : "Event", // Extracts title from Event model.
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, INK));
            movie.setAlignment(Element.ALIGN_CENTER);
            movie.setSpacingBefore(14);
            doc.add(movie);

            if (ev != null && ev.getCategory() != null && !ev.getCategory().isBlank()) { // Validation check.
                Paragraph cat = new Paragraph(ev.getCategory().toUpperCase(), // Adds category text.
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND));
                cat.setAlignment(Element.ALIGN_CENTER);
                cat.setSpacingAfter(6);
                doc.add(cat);
            }

            doc.add(hairline()); // Adds a dividing line (UI formatting in PDF).

            //  Movie Details 
            doc.add(sectionTitle("MOVIE DETAILS")); // Adds section heading.
            PdfPTable movieTbl = twoColTable(); // Creates a 2 column table layout.
            // Populates rows with Event and Venue data.
            addRow(movieTbl, "Movie",  ev != null ? ev.getTitle() : "—");
            addRow(movieTbl, "Screen", "Screen " + screenNumberFor(b.getId())); // Generates a screen number.
            addRow(movieTbl, "Date",   ev != null ? ev.getDate() : "—");
            addRow(movieTbl, "Time",   ev != null ? ev.getTime() : "—");
            addRow(movieTbl, "Venue",  venue != null
                    ? venue.getName() + (venue.getCity() != null && !venue.getCity().isBlank() ? ", " + venue.getCity() : "")
                    : "—");
            doc.add(movieTbl);

            doc.add(hairline()); // Adds another divider.

            //  Booking Details 
            doc.add(sectionTitle("BOOKING DETAILS"));
            PdfPTable bookTbl = twoColTable();
            // Populates rows with Booking and User data.
            addRow(bookTbl, "Booking ID", b.getId());
            addRow(bookTbl, "Name",       u != null ? (notBlank(u.getFullName()) ? u.getFullName() : u.getUsername()) : "—");
            addRow(bookTbl, "Seats",      notBlank(b.getSeats()) ? b.getSeats() : "—");
            addRow(bookTbl, "Status",     notBlank(b.getStatus()) ? b.getStatus().toUpperCase() : "—");
            addRow(bookTbl, "Total",      "Rs. " + formatPrice(b.getTotalPrice()));
            doc.add(bookTbl);

            //  Dashed tear line
            Paragraph spacerBefore = new Paragraph(" "); // Adds spacing.
            spacerBefore.setSpacingBefore(8);
            doc.add(spacerBefore);
            DottedLineSeparator dotted = new DottedLineSeparator(); // Creates visual dotted line.
            dotted.setLineColor(BRAND);
            dotted.setLineWidth(1.2f);
            dotted.setGap(4f);
            Paragraph tearLine = new Paragraph(new Chunk(dotted));
            tearLine.setSpacingAfter(8);
            doc.add(tearLine);

            //  QR Code (centered) 
            try { // ERROR HANDLING: Nested try catch specifically for QR generation.
                Image qrImg = Image.getInstance(generateQrPng(b.getId(), 240)); // Generates QR code image.
                qrImg.scaleAbsolute(110, 110);
                qrImg.setAlignment(Image.ALIGN_CENTER);
                doc.add(qrImg); // Adds QR to PDF.

                Paragraph scan = new Paragraph("Scan at the entrance",
                        FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED));
                scan.setAlignment(Element.ALIGN_CENTER);
                scan.setSpacingBefore(3);
                doc.add(scan);
            } catch (Exception qrEx) { // Fallback if QR generation fails.
                Paragraph fallback = new Paragraph("Booking ref: " + b.getId(),
                        FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED));
                fallback.setAlignment(Element.ALIGN_CENTER);
                doc.add(fallback);
            }

            //  Footer 
            Paragraph foot = new Paragraph("Enjoy Your Movie!", // Adds footer text.
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND_DARK));
            foot.setAlignment(Element.ALIGN_CENTER);
            foot.setSpacingBefore(10);
            doc.add(foot);

            Paragraph footSub = new Paragraph( // Adds terms/sub footer text.
                    "This ticket is non transferable. Please arrive 15 minutes before showtime.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, MUTED));
            footSub.setAlignment(Element.ALIGN_CENTER);
            footSub.setSpacingBefore(2);
            doc.add(footSub);

            doc.close(); // Finalizes and closes the PDF document.
            byte[] pdf = out.toByteArray(); // Converts the in memory stream to byte array.

            // Prepares HTTP headers to instruct the browser to download a PDF file.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket-" + b.getId() + ".pdf");
            headers.setContentLength(pdf.length);
            return ResponseEntity.ok().headers(headers).body(pdf); // Returns PDF file response to client.
        } catch (Exception e) {
            // ERROR HANDLING: Catches global PDF creation errors and returns 500 Server Error.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return ResponseEntity.status(500).headers(headers).body(("PDF error: " + e.getMessage()).getBytes());
        }
    }


    // OOP: Inner class inheriting from PdfPageEventHelper (Inheritance concept).
    private static class BorderEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) { // Triggered at the end of every page.
            PdfContentByte cb = writer.getDirectContentUnder();
            Rectangle pageSize = document.getPageSize();
            float margin = 30f;
            cb.saveState();
            cb.setColorStroke(BRAND); // Sets border color.
            cb.setLineWidth(1.6f); // Sets border width.
            cb.rectangle(margin, margin, // Draws rectangle based on margins.
                    pageSize.getWidth()  - 2 * margin,
                    pageSize.getHeight() - 2 * margin);
            cb.stroke();
            cb.restoreState();
        }
    }

    // helpers (Utility Methods for Clean Code)

    // Helper: Validates if a string is not null and not empty.
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    // Helper: Formats the double price into a 2 decimal string format.
    private static String formatPrice(double v) {
        return String.format("%,.2f", v);
    }

    // Helper: Uses ZXing library to generate a QR Code based on the booking ID payload.
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

    // Helper: Generates a pseudo random screen number (1 to 8) deterministically based on Booking ID.
    private static int screenNumberFor(String bookingId) {
        if (bookingId == null || bookingId.isEmpty()) return 1;
        int h = 0;
        for (int i = 0; i < bookingId.length(); i++) h = (h * 31 + bookingId.charAt(i)) & 0x7fffffff;
        return (h % 8) + 1;
    }

    // Helper: Creates a consistent 2 column layout table for details sections.
    private static PdfPTable twoColTable() {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{1.0f, 1.7f}); } catch (Exception ignored) {} // Sets column width ratio.
        t.setSpacingBefore(2);
        t.setSpacingAfter(2);
        return t;
    }

    // Helper: Adds a formatted row (Label, Value) to a given table.
    private static void addRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label.toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, MUTED)));
        PdfPCell v = new PdfPCell(new Phrase(value == null ? "" : value,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, INK)));
        l.setBorder(Rectangle.NO_BORDER); v.setBorder(Rectangle.NO_BORDER); // Removes cell borders for clean UI.
        l.setPaddingTop(5); l.setPaddingBottom(5); l.setPaddingLeft(2); l.setPaddingRight(6);
        v.setPaddingTop(5); v.setPaddingBottom(5); v.setPaddingLeft(2); v.setPaddingRight(2);
        l.setVerticalAlignment(Element.ALIGN_MIDDLE);
        v.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(l); table.addCell(v); // Adds cells to row.
    }

    // Helper: Standardizes the styling for section titles inside the PDF.
    private static Paragraph sectionTitle(String txt) {
        Paragraph p = new Paragraph(txt,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND));
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        return p;
    }

    // Helper: Creates a horizontal dividing line for the PDF.
    private static Paragraph hairline() {
        LineSeparator ls = new LineSeparator(0.7f, 100, HAIRLINE, Element.ALIGN_CENTER, 0);
        Paragraph p = new Paragraph(new Chunk(ls));
        p.setSpacingBefore(6);
        p.setSpacingAfter(2);
        return p;
    }
}