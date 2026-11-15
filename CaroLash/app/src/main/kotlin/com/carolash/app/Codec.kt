package com.carolash.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Convierte los datos a texto JSON y de vuelta. */
object Codec {
    fun encode(d: AppData): String {
        val o = JSONObject()
        val s = d.settings
        o.put("settings", JSONObject().apply {
            put("onboarded", s.onboarded)
            put("workDays", JSONArray(s.workDays.sorted()))
            put("startMin", s.startMin)
            put("endMin", s.endMin)
            put("breakMin", s.breakMin)
            put("lunch", s.lunch)
            put("goalIncome", s.goalIncome)
            put("goalWeek", s.goalWeek)
            put("goalMonth", s.goalMonth)
            put("maintenanceAlerts", s.maintenanceAlerts)
        })
        o.put("services", JSONArray().also { arr ->
            d.services.forEach {
                arr.put(JSONObject().put("id", it.id).put("name", it.name).put("price", it.price).put("minutes", it.minutes))
            }
        })
        o.put("clients", JSONArray().also { arr ->
            d.clients.forEach {
                arr.put(JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("phone", it.phone)
                    put("eyes", JSONArray(it.eyes))
                    put("design", it.design)
                    put("style", it.style)
                    put("curve", it.curve)
                    put("thickness", it.thickness)
                    put("map", JSONArray(it.map))
                    put("weeks", it.weeks)
                    put("allergy", it.allergy)
                    put("createdOn", it.createdOn.toString())
                })
            }
        })
        o.put("appointments", JSONArray().also { arr ->
            d.appointments.forEach {
                arr.put(JSONObject().apply {
                    put("id", it.id)
                    put("clientId", it.clientId)
                    put("service", it.service)
                    put("price", it.price)
                    put("date", it.date.toString())
                    put("startMin", it.startMin)
                    put("minutes", it.minutes)
                    put("status", it.status.name)
                    put("reminded", it.reminded)
                })
            }
        })
        o.put("seenBadges", JSONArray(d.seenBadges.toList()))
        return o.toString()
    }

    private fun ints(a: JSONArray?): List<Int> {
        if (a == null) return emptyList()
        return (0 until a.length()).map { a.getInt(it) }
    }

    fun decode(text: String): AppData {
        val o = JSONObject(text)
        val so = o.optJSONObject("settings")
        val base = Settings()
        val settings = if (so == null) base else Settings(
            onboarded = so.optBoolean("onboarded", false),
            workDays = ints(so.optJSONArray("workDays")).toSet().ifEmpty { base.workDays },
            startMin = so.optInt("startMin", base.startMin),
            endMin = so.optInt("endMin", base.endMin),
            breakMin = so.optInt("breakMin", base.breakMin),
            lunch = so.optBoolean("lunch", base.lunch),
            goalIncome = so.optInt("goalIncome", base.goalIncome),
            goalWeek = so.optInt("goalWeek", base.goalWeek),
            goalMonth = so.optInt("goalMonth", base.goalMonth),
            maintenanceAlerts = so.optBoolean("maintenanceAlerts", true),
        )
        val sv = o.optJSONArray("services")
        val services = if (sv == null) defaultServices() else (0 until sv.length()).map {
            val x = sv.getJSONObject(it)
            Service(x.getString("id"), x.getString("name"), x.getInt("price"), x.getInt("minutes"))
        }
        val cl = o.optJSONArray("clients")
        val clients = if (cl == null) emptyList() else (0 until cl.length()).map {
            val x = cl.getJSONObject(it)
            Client(
                id = x.getString("id"),
                name = x.getString("name"),
                phone = x.optString("phone", ""),
                eyes = ints(x.optJSONArray("eyes")),
                design = x.optInt("design", 1),
                style = x.optString("style", ""),
                curve = x.optString("curve", ""),
                thickness = x.optString("thickness", ""),
                map = ints(x.optJSONArray("map")).ifEmpty { PRESETS[1] },
                weeks = x.optInt("weeks", 3),
                allergy = x.optString("allergy", ""),
                createdOn = LocalDate.parse(x.getString("createdOn")),
            )
        }
        val ap = o.optJSONArray("appointments")
        val appts = if (ap == null) emptyList() else (0 until ap.length()).map {
            val x = ap.getJSONObject(it)
            Appointment(
                id = x.getString("id"),
                clientId = x.getString("clientId"),
                service = x.getString("service"),
                price = x.getInt("price"),
                date = LocalDate.parse(x.getString("date")),
                startMin = x.getInt("startMin"),
                minutes = x.getInt("minutes"),
                status = try { Status.valueOf(x.getString("status")) } catch (e: Exception) { Status.PENDING },
                reminded = x.optBoolean("reminded", false),
            )
        }
        val sb = o.optJSONArray("seenBadges")
        val seen = if (sb == null) emptySet() else (0 until sb.length()).map { sb.getString(it) }.toSet()
        return AppData(settings, services, clients, appts, seen)
    }
}
