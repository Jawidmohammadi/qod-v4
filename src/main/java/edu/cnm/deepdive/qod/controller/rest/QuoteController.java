package edu.cnm.deepdive.qod.controller.rest;

import edu.cnm.deepdive.qod.model.entity.Quote;
import edu.cnm.deepdive.qod.model.entity.Source;
import edu.cnm.deepdive.qod.model.entity.User;
import edu.cnm.deepdive.qod.service.QuoteService;
import java.time.LocalDate;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quotes")
@ExposesResourceFor(Quote.class)
@Validated
public class QuoteController {

  private final QuoteService quoteService;

  @Autowired
  public QuoteController(QuoteService quoteService) {
    this.quoteService = quoteService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Quote> post(@RequestBody @Valid Quote quote, Authentication auth) {
    User user = (User) auth.getPrincipal();
    quote.setContributer(user);
    quote = quoteService.create(quote);
    return ResponseEntity.created(quote.getHref()).body(quote);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Quote> get() {
    return quoteService.readAll();
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Quote> search(@RequestParam @Size(min = 3) String q) {
    return quoteService.readFiltered(q);
  }

  @GetMapping(value = "/random", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Quote> getRandom() {
    return ResponseEntity.of(quoteService.readRandom());
  }

  @GetMapping(value = "/qod", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Quote> getQuoteOfDay(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.of(quoteService.readDaily(date));
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Quote> get(@PathVariable UUID id) {
    return ResponseEntity.of(quoteService.readOne(id));
  }

  @PutMapping(value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Quote> put(@PathVariable UUID id, @RequestBody Quote quote) {
    return ResponseEntity.of(quoteService.update(id, quote));
  }

  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    quoteService.delete(id);
  }

  @GetMapping(value = "/{id}/text", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getText(@PathVariable UUID id) {
    return ResponseEntity.of(quoteService.readOne(id).map(Quote::getText));
  }

  @PutMapping(value = "/{id}/text",
      consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> putText(@PathVariable UUID id, @RequestBody @NotBlank String modifiedQuote) {
    return ResponseEntity.of(quoteService.update(id, modifiedQuote));
  }

  @GetMapping(value = "/{id}/source", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Source> getAttribution(@PathVariable UUID id) {
    return ResponseEntity.of(quoteService.readOne(id).map(Quote::getSource));
  }

  @PutMapping(value = "/{quoteId}/source",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Source> putAttribution(@PathVariable UUID quoteId, @RequestBody @Valid Source source) {
    return ResponseEntity.of(quoteService.update(quoteId, source));
  }

  @PutMapping(value = "/{quoteId}/source",
      consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<UUID> putAttribution(@PathVariable UUID quoteId, @RequestBody UUID sourceId) {
    return ResponseEntity.of(quoteService.update(quoteId, sourceId).map(Source::getId));
  }

  @DeleteMapping(value = "/{quoteId}/source")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clearAttribution(@PathVariable UUID quoteId) {
    quoteService.clearSource(quoteId);
  }

}
