package dev.fixroom.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController {
    private final CheckoutService checkout;
    public CheckoutController(CheckoutService checkout) { this.checkout = checkout; }

    public record QuoteRequest(@Min(0) @Max(100000000) long subtotalCents,
                               @Min(0) @Max(100) int couponPercent) {}

    @PostMapping("/api/checkout/quote")
    public CheckoutService.Quote quote(@Valid @RequestBody QuoteRequest request) {
        return checkout.quote(request.subtotalCents(), request.couponPercent());
    }
}
