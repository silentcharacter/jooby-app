package com.mycompany.service.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import com.mycompany.domain.shop.*;
import com.mycompany.service.AbstractService;
import com.mycompany.service.AuditService;
import com.mycompany.util.Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.jongo.MongoCursor;
import org.jooby.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class OrderService extends AbstractService<Order> {

    private final static Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private ObjectMapper mapper = new ObjectMapper();
    @Inject
    private CartService cartService;
    @Inject
    private DeliveryTypeService deliveryTypeService;
    @Inject
    private PaymentTypeService paymentTypeService;
    @Inject
    private SauceService sauceService;
    @Inject
    private ProductService productService;
    @Inject
    private UnitService unitService;
    @Inject
    private ColorService colorService;
    @Inject
    private CustomerService customerService;
    @Inject
    private MongoDatabase db;
    @Inject
    private GoogleMapsService googleMapsService;
    @Inject
    private DistrictService districtService;
    @Inject
    private GlobalConfigService configService;
    @Inject
    private AuditService auditService;

    public OrderService() {
        super(Order.class);
    }

    private String generateNewOrderNumber() {
        Document doc = db.runCommand(new Document("$eval", "getNextSequence('orderNumber')"));
        String result = String.valueOf(doc.getDouble("retval").intValue());
        while (result.length() < 8) {
            result = "0" + result;
        }
        return result;
    }

    public Map<String, Object> findByPhone(String phone) {
        Map<String, Object> map = new HashMap<>();
        if (Strings.isNullOrEmpty(phone)) {
            return map;
        }
        Order order = getBy("phone", phone);
        map.put("name", order.name);
        map.put("streetName", order.streetName);
        map.put("streetNumber", order.streetNumber);
        map.put("flat", order.flat);
        map.put("entrance", order.entrance);
        return map;
    }

    public Order placeOrder(Request req) {
        Optional<String> cartJson = req.session().get("cart").toOptional();
        if (!cartJson.isPresent()) {
            return null;
        }
        try {
            Order order = mapper.readValue(cartJson.get(), Order.class);
            order.testOrder = BooleanUtils.isTrue(configService.getConfig().testMode);
            order.channel = Channel.ONLINE;
            order.orderNumber = generateNewOrderNumber();
            order.orderDate = new Date();
            order.status = OrderStatus.NEW;
            parseStreetNumber(order);
            //needed to solve ng-admin bug not showing embedded linked entities
            order.sauces = sauceService.getAll().stream().map(sauce -> sauce.id).collect(Collectors.toList());
            insert(order);

            return order;
        } catch (IOException e) {
            logger.error("Error placing order", e);
        }
        return null;
    }

    private void parseStreetNumber(Order order) {
        String[] parts = order.originalStreetNumber.toLowerCase().split("к");
        if (parts.length > 1) {
            order.korpus = Integer.valueOf(parts[1].replaceAll("[^\\d.]", ""));
        }
        parts = parts[0].split("/");
        order.streetNumber = Integer.valueOf(parts[0].replaceAll("[^\\d.]", ""));
        order.litera = parts[0].replaceAll("[\\d.]", "");
    }

    public Order createOrder(Map map) throws ParseException {
        map.put("orderDate", new Date());
        map.put("deliveryDate", FORMAT.parse((String) map.get("deliveryDate")));
        map.remove("id");
        Order order = mapper.convertValue(map, Order.class);
        order.channel = Channel.PHONE;
        order.testOrder = BooleanUtils.isTrue(configService.getConfig().testMode);
        order.orderNumber = generateNewOrderNumber();
        order.orderDate = new Date();
        order.status = OrderStatus.IN_DELIVERY;
        //needed to solve ng-admin bug not showing embedded linked entities
        order.sauces = sauceService.getAll().stream().map(sauce -> sauce.id).collect(Collectors.toList());
        insert(order);

        return order;
    }

    private void linkToCustomer(Order order) {
        order.phone = Utils.formatPhone(order.phone);
        Customer customer = customerService.getBy("phone", order.phone);
        if (customer == null) {
            customer = new Customer();
            customer.name = order.name;
            customer.phone = order.phone;
            customer.addresses = Collections.singletonList(
                    new Address(order.streetName, order.streetNumber, order.litera, order.korpus, order.entrance, order.flat));
            customer.totalOrdered = order.totalPrice;
            customer = customerService.insert(customer);
        } else {
            Optional<Address> address = customer.addresses.stream().filter(a -> a.streetName.equals(order.streetName)
                    && a.streetNumber.equals(order.streetNumber) && a.flat.equals(order.flat)).findFirst();
            if (!address.isPresent()) {
                customer.addresses.add(new Address(order.streetName, order.streetNumber, order.litera, order.korpus, order.entrance, order.flat));
            }
            customer.totalOrdered = getAll(0, Integer.MAX_VALUE, "", "{customerId:#}", Collections.singletonList(customer.id)).stream()
                    .filter(o -> !o.orderNumber.equals(order.orderNumber)).mapToInt(o -> o.totalPrice).sum() + order.totalPrice;
            customerService.update(customer);
        }
        order.customerId = customer.id;
    }

    public Map<String, Object> getFetchedOrder(String orderId) {
        return getOrderMap(getById(orderId));
    }

    public Map<String, Object> getFetchedOrderByNumber(String orderNumber) {
        return getFetchedOrderByNumber(orderNumber, null);
    }

    public Map<String, Object> getFetchedOrderByNumber(String orderNumber, Order voiceOrder) {
        if ("new".equals(orderNumber)) {
            return getOrderMap(populateWIthVoiceOrder(cartService.getNewCart(), voiceOrder));
        }
        return getOrderMap(getBy("orderNumber", orderNumber));
    }

    private Cart populateWIthVoiceOrder(Cart newCart, Order voiceOrder) {
        if (voiceOrder == null) {
            return newCart;
        }
        newCart.entries = voiceOrder.entries;
        newCart.name = voiceOrder.name;
        newCart.streetName = voiceOrder.streetName;
        newCart.streetNumber = voiceOrder.streetNumber;
        newCart.flat = voiceOrder.flat;
        newCart.calculate();
        return newCart;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOrderMap(Cart cart) {
        return getOrderMap(cart, true);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOrderMap(Cart cart, boolean withEntries) {
        Map<String, Object> map = mapper.convertValue(cart, Map.class);
        map.put("delivery", deliveryTypeService.getById(cart.deliveryId));
        map.put("paymentType", paymentTypeService.getById(cart.paymentTypeId));
        if (map.containsValue("deliveryDate")) {
            map.put("deliveryDate", new Date((Long) map.get("deliveryDate")));
        }
        if (withEntries) {
            List<Map> entries = (List) map.get("entries");
            entries.forEach(entry -> {
                Product product = productService.getById((String) entry.get("productId"));
                product.image = null;
                Map<String, Object> productMap = mapper.convertValue(product, Map.class);
                if (StringUtils.isNotEmpty(product.unitId)) {
                    productMap.put("unit", unitService.getById(product.unitId));
                }
                entry.put("product", productMap);
                if (entry.get("colorId") != null) {
                    entry.put("color", colorService.getById((String) entry.get("colorId")));
                }
                if (entry.get("sauces") != null) {
                    List<String> sauces = (List) entry.get("sauces");
                    entry.put("sauces", sauces.stream().map(sauceService::getById).collect(Collectors.toList()));
                }
            });
        } else {
            map.put("entries", Collections.emptyList());
        }

        if (cart instanceof Order) {
            Order order = (Order) cart;
            if (order.districtId != null)
                map.put("districtName", districtService.getByIdShort(((Order) cart).districtId).name);
        }
        return map;
    }

    public Map updateOrder(Map<String, Object> order, Request req) throws ParseException {
        Order saved = getById(Utils.getValue(order, "id"));
        auditService.logEvent(req, saved);
        Boolean toDelivery = Utils.getValue(order, "toDelivery");
        if (BooleanUtils.isTrue(toDelivery)) {
            saved.status = OrderStatus.IN_DELIVERY;
        } else if (saved.status == OrderStatus.NEW) {
            saved.status = OrderStatus.CLARIFICATION;
        }
        saved.deliveryDate = FORMAT.parse((String) order.get("deliveryDate"));
        saved.deliveryId = Utils.getValue(order, "delivery.id");
        saved.deliveryTime = Utils.getValue(order, "deliveryTime");
        saved.streetName = Utils.getValue(order, "streetName");
        saved.litera = Utils.getValue(order, "litera");
        saved.korpus = Utils.getValue(order, "korpus");
        saved.streetNumber = Utils.getValue(order, "streetNumber");
        saved.entrance = Utils.getValue(order, "entrance");
        saved.flat = Utils.getValue(order, "flat");
        saved.comment = Utils.getValue(order, "comment");
        update(saved);
        return getFetchedOrder(saved.id);
    }

    @Override
    public void onSave(Order order) {
        updateOriginalStreetNumber(order);
        cartService.calculateCart(order);
        linkToCustomer(order);
        updateCoordinates(order);
    }

    @Override
    public void onRemove(String id) {
        throw new IllegalArgumentException("Not allowed to remove orders");
    }

    private void updateOriginalStreetNumber(Order order) {
        if (order.channel == null) {
            order.channel = Channel.PHONE;
        }
        if (order.channel == Channel.PHONE) {
            StringBuilder sb = new StringBuilder(String.valueOf(order.streetNumber));
            sb.append(StringUtils.defaultString(order.litera));
            if (order.korpus != null) {
                sb.append(" корп. ").append(String.valueOf(order.korpus));
            }
            order.originalStreetNumber = sb.toString();
        }
    }

    private void updateCoordinates(Order order) {
        Geometry geometry = googleMapsService.getCoordinates(order.streetName, order.streetNumber, order.litera, order.korpus);
        if (geometry != null) {
            order.lat = geometry.getLat();
            order.lng = geometry.getLng();
            District district = googleMapsService.defineDistrict(geometry);
            order.districtId = district != null ? district.id : null;
        }
    }

    public Map cancelOrder(String id, Request req) {
        Order order = getById(id);
        auditService.logEvent(req, order);
        order.status = OrderStatus.CANCELLED;
        update(order);
        return getFetchedOrder(order.id);
    }

    public List<Map<String, Object>> getDeliverySchedule(String dateStr, String time) {
        Date dateStart = Utils.safeParseUTC(dateStr);
        Date dateEnd = new Date(dateStart.getTime() + 1000 * 3600 * 24);
        MongoCursor<Order> cursor = getCollection()
                .find("{deliveryDate:{$gte:#,$lt:#}, deliveryTime:#}", dateStart, dateEnd, time).as(Order.class);
        return StreamSupport.stream(cursor.spliterator(), false).map(this::getOrderMap).collect(Collectors.toList());
    }

    public Order parseOrderFromString(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        Order order = new Order();
        order.entries = new ArrayList<>();
        String[] parts = content.split("адрес|имя");
        Pattern pattern = Pattern.compile("(.*?)(\\d+)");
        Matcher matcher = pattern.matcher(parts[0]);
        int i = 0;
        while (matcher.find()) {
            List<Product> found = productService.fullTextSearch(matcher.group(1).trim().toLowerCase());
            if (found.isEmpty()) {
                continue;
            }
            order.entries.add(new OrderEntry(found.get(0), Integer.valueOf(matcher.group(2)), null, null, i++));
        }
        pattern = Pattern.compile("(.*?)(\\d+)([\\s,а,б,в,г,д,е,ж]+)(\\d+)");
        matcher = pattern.matcher(parts[1]);
        if (matcher.find()) {
            order.streetName = matcher.group(1);
            try {
                //todo: test it!
                order.streetNumber = Integer.valueOf(matcher.group(2));
                order.litera = matcher.group(3).trim();
                order.flat = Integer.valueOf(matcher.group(4));
            } catch (NumberFormatException ex) {
                logger.error("error parsing order from string", ex);
            }
        }
        order.name = parts[2];
        return order;
    }

}
