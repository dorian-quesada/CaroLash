package com.carolash.app

import java.time.LocalDate

/** Un servicio con su precio (en colones) y duración. */
data class Service(
    val id: String,
    val name: String,
    val price: Int,
    val minutes: Int,
)

data class Client(
    val id: String,
    val name: String,
    val phone: String,
    /** Índices de EYES (forma de ojos). */
    val eyes: List<Int>,
    /** 0 = muñeca, 1 = ardilla, 2 = ojo de gato. */
    val design: Int,
    val style: String,
    val curve: String,
    val thickness: String,
    /** Mapping: largo en mm de cada zona, de la esquina interna a la externa. */
    val map: List<Int>,
    /** Cada cuántas semanas le toca mantenimiento. */
    val weeks: Int,
    val allergy: String,
    val createdOn: LocalDate,
)

enum class Status { PENDING, CONFIRMED, DONE, CANCELED }

data class Appointment(
    val id: String,
    val clientId: String,
    val service: String,
    val price: Int,
    val date: LocalDate,
    /** Minutos desde las 00:00. */
    val startMin: Int,
    val minutes: Int,
    val status: Status,
    val reminded: Boolean,
)

data class Settings(
    val onboarded: Boolean = false,
    /** 1 = lunes ... 7 = domingo. */
    val workDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6),
    val startMin: Int = 8 * 60,
    val endMin: Int = 18 * 60,
    val breakMin: Int = 15,
    val lunch: Boolean = true,
    val goalIncome: Int = 250_000,
    val goalWeek: Int = 4,
    val goalMonth: Int = 16,
    val maintenanceAlerts: Boolean = true,
)

data class AppData(
    val settings: Settings = Settings(),
    val services: List<Service> = defaultServices(),
    val clients: List<Client> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    /** Insignias cuya celebración ya se mostró. */
    val seenBadges: Set<String> = emptySet(),
)

fun defaultServices(): List<Service> = listOf(
    Service("s1", "Pestañas clásicas", 25_000, 120),
    Service("s2", "Pestañas híbridas", 30_000, 150),
    Service("s3", "Volumen ruso", 35_000, 180),
    Service("s4", "Mantenimiento", 18_000, 90),
)

fun newId(): String = java.util.UUID.randomUUID().toString().take(8)
