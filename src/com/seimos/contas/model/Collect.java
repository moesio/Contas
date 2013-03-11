package com.seimos.contas.model;

import java.util.Calendar;

public class Collect {
	private Integer id;
	private Calendar date;
	private Boolean sent;
	private Double om;
	private Double cmsr;
	private Double dc;

	public Integer getId() {
		return id;
	}

	public Collect setId(Integer id) {
		this.id = id;
		return this;
	}

	public Calendar getDate() {
		return date;
	}

	public Collect setDate(Calendar date) {
		this.date = date;
		return this;
	}

	public Boolean getSent() {
		return sent;
	}

	public Collect setSent(Boolean sent) {
		this.sent = sent;
		return this;
	}

	public Double getOm() {
		return om;
	}

	public Collect setOm(Double om) {
		this.om = om;
		return this;
	}

	public Double getCmsr() {
		return cmsr;
	}

	public Collect setCmsr(Double cmsr) {
		this.cmsr = cmsr;
		return this;
	}

	public Double getDc() {
		return dc;
	}

	public Collect setDc(Double dc) {
		this.dc = dc;
		return this;
	}

	@Override
	public String toString() {
		return "Collect [id="
				+ id
				+ ", date="
				+ (date == null ? null : date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-"
						+ date.get(Calendar.DATE)) + ", om=" + om + ", cmsr=" + cmsr + ", dc=" + dc + "]";
	}

}
